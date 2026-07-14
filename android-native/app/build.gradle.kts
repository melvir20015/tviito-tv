plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Single source of truth for the app version: the top-level VERSION file
// (repo root). versionName is read verbatim ("x.y.z") and versionCode is
// DERIVED deterministically as major*10000 + minor*100 + patch. This matches
// the packed-int scheme UpdateChecker.kt uses to compare GitHub release tags,
// so the in-app updater and the build stay in lockstep with one edit.
// 1.0.29 → versionName "1.0.29", versionCode 10029 (> the legacy code 39, so
// installs over existing builds stay monotonic).
val versionFile = rootProject.file("../VERSION")
val appVersionName: String = versionFile.readText().trim()
val appVersionCode: Int = run {
    val parts = appVersionName.split(".").map { it.trim().toInt() }
    require(parts.size >= 3) { "VERSION must be x.y.z, got '$appVersionName'" }
    parts[0] * 10_000 + parts[1] * 100 + parts[2]
}

// Optional remote endpoints. Defaults are intentionally empty/disabled so a
// plain Tviito TV build never phones home to the external telemetry endpoint or
// release feed. Private builds may opt in via Gradle properties or
// environment variables without hardcoding secrets in source.
fun resolveBuildConfigValue(name: String, default: String): String =
    (project.findProperty(name) as String?)?.takeIf { it.isNotBlank() }
        ?: System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: default

val tviitoLogUrl = resolveBuildConfigValue("TVIITO_LOG_URL", "")
val tviitoLogToken = resolveBuildConfigValue("TVIITO_LOG_TOKEN", "")
val tviitoUpdateRepo = resolveBuildConfigValue("TVIITO_UPDATE_REPO", "")
val tviitoUpdateApkName = resolveBuildConfigValue("TVIITO_UPDATE_APK_NAME", "")
val tviitoAutoUpdateEnabled = resolveBuildConfigValue("TVIITO_AUTO_UPDATE_ENABLED", "false")
    .equals("true", ignoreCase = true)

android {
    namespace = "com.ultratv.tv.nativeapp"
    compileSdk = 35

    defaultConfig {
        // Different applicationId during development so it can be installed
        // alongside the existing Capacitor build (com.ultratv.tv).
        applicationId = "com.tviito.tv"
        minSdk = 28
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
        vectorDrawables { useSupportLibrary = true }

        // Optional telemetry/update config — see resolveBuildConfigValue() above.
        // Consumed by RemoteLog and UpdateChecker. String values must be wrapped in escaped quotes.
        buildConfigField("String", "LOG_URL", "\"$tviitoLogUrl\"")
        buildConfigField("String", "LOG_TOKEN", "\"$tviitoLogToken\"")
        buildConfigField("String", "UPDATE_REPO", "\"$tviitoUpdateRepo\"")
        buildConfigField("String", "UPDATE_APK_NAME", "\"$tviitoUpdateApkName\"")
        buildConfigField("boolean", "AUTO_UPDATE_ENABLED", tviitoAutoUpdateEnabled.toString())
    }

    // Release signing — reads TVIITO_KEYSTORE / TVIITO_KEYSTORE_PASSWORD /
    // TVIITO_KEY_ALIAS / TVIITO_KEY_PASSWORD env vars (with TVIITO_LINEAGE for the
    // rotation lineage). Falls back to the debug keystore when env vars are
    // missing so a fresh checkout still produces an installable APK in CI / dev.
    // See SECURITY.md for the rotation procedure.
    //
    // AGP doesn't expose signingLineage in the DSL, so we hand-roll a
    // post-build task `signRelease` that re-signs the produced APK with
    // apksigner --lineage. The end result is an APK that carries the
    // proof-of-rotation signing block, allowing it to install over the
    // existing debug-key install without "INSTALL_FAILED_UPDATE_INCOMPATIBLE".
    signingConfigs {
        create("release") {
            val ksPath = System.getenv("TVIITO_KEYSTORE")
            if (!ksPath.isNullOrBlank() && file(ksPath).exists()) {
                storeFile = file(ksPath)
                storePassword = System.getenv("TVIITO_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("TVIITO_KEY_ALIAS")
                keyPassword = System.getenv("TVIITO_KEY_PASSWORD")
                // Rotation lineage is only natively supported by APK Signature
                // Scheme v3 (Android 9 / API 28+). Pre-9 devices would need
                // the OLD signer for v1/v2 — which we don't ship — so we
                // bumped minSdk to 28 and disable v1/v2. Modern Android TV
                // boxes are all on 9+.
                enableV1Signing = false
                enableV2Signing = false
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        release {
            // R8 full-mode: shrinks resources + obfuscates code. ~18 MB → ~8 MB.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // AGP always signs the release output with the debug keystore.
            // The `resignRelease` Gradle task (further down) then re-signs
            // the produced APK with the proper upload key and embeds the
            // rotation lineage via apksigner. This roundabout works because
            // apksigner can't re-sign an APK that already has v3 signatures
            // from the new key with an added lineage — it needs the old
            // (debug) key as the starting point.
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.tv.material3.ExperimentalTvMaterial3Api",
            "-opt-in=androidx.media3.common.util.UnstableApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets["main"].kotlin.srcDirs("src/main/kotlin")
    sourceSets["test"].kotlin.srcDirs("src/test/kotlin")

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    // Lint Vital runs during assembleRelease and blocks on any "error" severity
    // issue. We're shipping a hobby APK with no Play track, and the errors it
    // raises are typically about resource configurations that don't affect
    // runtime — flip abortOnError off and only fail the build on actual code
    // issues (caught by the compiler).
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        // Workaround mínimo para el crash de lint/Compose con Kotlin 2.0.21:
        // algunos detectores basados en UAST fallan con Kotlin 2.0.21 y AGP 8.7.3 con
        // "KaSimpleVariableAccessCall, but interface was expected" antes de
        // emitir hallazgos. Deshabilitamos solo esos detectores para conservar el
        // resto de verificaciones de lint activo.
        disable += setOf(
            "FrequentlyChangingValue",
            "RememberInComposition",
            "AutoboxingStateCreation",
            "NullSafeMutableLiveData",
        )
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE*",
            "META-INF/NOTICE*",
        )
    }
}

// Export Room schemas so future version bumps can ship verified Migration
// objects (and so the schema history is tracked in VCS under app/schemas).
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.tv.foundation)
    implementation(libs.compose.tv.material)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)
    implementation(libs.media3.cast)
    implementation(libs.media3.datasource.rtmp)
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation(libs.mediarouter)
    implementation(libs.play.cast.framework)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.coil.compose)
    implementation(libs.coroutines.android)
    implementation(libs.serialization.json)
    implementation(libs.datastore.preferences)
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    implementation(libs.room.paging)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Tests — runs on the local JVM with Robolectric for Android types we
    // can't easily strip out (android.util.Base64).
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.13")
    // Robolectric intenta descargar este artefacto en tiempo de ejecución; lo
    // declaramos como dependencia de test para que Gradle lo resuelva antes y
    // los tests puedan ejecutarse aun cuando el sandbox de test no tenga red.
    testImplementation("org.robolectric:android-all-instrumented:14-robolectric-10818077-i6")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.json:json:20240303")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

val prepareRobolectricOfflineDeps by tasks.registering(Copy::class) {
    val debugUnitTestRuntime = configurations.named("debugUnitTestRuntimeClasspath")
    from(debugUnitTestRuntime) {
        include("android-all-instrumented-*.jar")
    }
    into(layout.buildDirectory.dir("robolectric-offline-deps"))
}

tasks.withType<Test>().configureEach {
    dependsOn(prepareRobolectricOfflineDeps)
    // Evita que Robolectric intente descargar android-all dentro del proceso
    // de test, donde algunos entornos bloquean red aunque Gradle ya resolvió
    // dependencias. La carpeta se rellena desde testRuntimeClasspath arriba.
    systemProperty("robolectric.offline", "true")
    systemProperty(
        "robolectric.dependency.dir",
        layout.buildDirectory.dir("robolectric-offline-deps").get().asFile.absolutePath,
    )
}

/**
 * Post-process release APK with apksigner --lineage so it installs in place
 * over the existing debug-signed release. Only runs when the env vars are
 * set; otherwise it's a no-op (CI / dev keep using the debug fallback).
 */
val resignRelease by tasks.registering {
    dependsOn("assembleRelease")
    // The doLast block holds script-object references (file(), env lookups)
    // that Gradle's configuration cache can't serialize — opt out explicitly.
    notCompatibleWithConfigurationCache("hand-rolled apksigner exec")
    doLast {
        val ks = System.getenv("TVIITO_KEYSTORE") ?: return@doLast
        val ksPwd = System.getenv("TVIITO_KEYSTORE_PASSWORD") ?: return@doLast
        val alias = System.getenv("TVIITO_KEY_ALIAS") ?: return@doLast
        val keyPwd = System.getenv("TVIITO_KEY_PASSWORD") ?: ksPwd
        val lineage = System.getenv("TVIITO_LINEAGE") ?: return@doLast

        val apk = file("build/outputs/apk/release/app-release.apk")
        if (!apk.exists()) {
            println("[resignRelease] APK not found at $apk")
            return@doLast
        }
        // Locate apksigner — prefer the build-tools that match compileSdk.
        val sdkRoot = System.getenv("ANDROID_HOME")
            ?: System.getenv("ANDROID_SDK_ROOT")
            ?: "${System.getProperty("user.home")}/Library/Android/sdk"
        val buildTools = file("$sdkRoot/build-tools").listFiles()
            ?.sortedByDescending { it.name }
            ?.firstOrNull { File(it, "apksigner").canExecute() }
            ?: error("[resignRelease] apksigner not found under $sdkRoot/build-tools")
        val apksigner = "${buildTools.absolutePath}/apksigner"

        val proc = ProcessBuilder(
            apksigner, "sign",
            "--ks", ks,
            "--ks-key-alias", alias,
            "--ks-pass", "pass:$ksPwd",
            "--key-pass", "pass:$keyPwd",
            "--lineage", lineage,
            "--rotation-min-sdk-version", "28",
            "--min-sdk-version", "28",
            "--v1-signing-enabled", "false",
            "--v2-signing-enabled", "false",
            "--v3-signing-enabled", "true",
            "--v4-signing-enabled", "true",
            apk.absolutePath,
        ).inheritIO().start()
        val code = proc.waitFor()
        check(code == 0) { "[resignRelease] apksigner exited with $code" }
        println("[resignRelease] APK re-signed with rotation lineage → $apk")
    }
}
