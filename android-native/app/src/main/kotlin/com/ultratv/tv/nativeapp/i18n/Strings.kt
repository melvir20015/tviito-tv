package com.ultratv.tv.nativeapp.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import java.util.EnumMap

/**
 * Lightweight inline translation table. We deliberately avoid the Android
 * resource framework because most of the UI text is inline in Compose;
 * externalising every literal would be a multi-day rewrite. Strings not in
 * the table fall through to their English literal — partial coverage is
 * better than no coverage.
 */
enum class AppLang(val code: String, val displayName: String, val rtl: Boolean = false) {
    System("system", "System (auto)"),
    English("en", "English"),
    French("fr", "Français"),
    Spanish("es", "Español"),
    Arabic("ar", "العربية", rtl = true);

    companion object {
        fun fromCode(code: String): AppLang = entries.firstOrNull { it.code == code } ?: System
    }
}

enum class StringKey(val defaultValue: String) {
    NavHome("Home"),
    NavLive("Live TV"),
    NavGuide("Guide"),
    NavMovies("Movies"),
    NavSeries("Series"),
    NavFavorites("Favorites"),
    NavSearch("Search"),
    NavCategories("Categories"),
    NavMultiview("Multi-View"),
    NavRecordings("Recordings"),
    NavSettings("Settings"),
    HomeWelcome("Welcome to Tviito TV"),
    HomeSubtitle("Native build · D-pad ready"),
    HomeContinueWatching("Continue watching"),
    HomeRecentlyWatched("Recently watched"),
    HomeFeaturedChannels("Featured channels"),
    HomeFeaturedMovies("Movies"),
    HomeFeaturedSeries("Series"),
    OnboardingMacLabel("Your device MAC:"),
    OnboardingOpenSettings("Open Settings"),
    OnboardingFirstTime("First-time setup"),
    OnboardingTwoPaths("Two paths to add a provider:"),
    OnboardingPathManual("1. Open Settings → +Xtream / +M3U / +M3U file / +Stalker, fill in the form."),
    OnboardingPathCloud("2. Self-host the Cloudflare Worker from cloudflare-config/, paste this MAC in its dashboard, then in Settings → Set worker URL → Sync from cloud."),
    ParentalPinEnabled("Parental PIN: enabled (4-digit)"),
    ParentalPinNotSet("Parental PIN: not set"),
    ParentalSetPin("Set PIN"),
    ParentalChangePin("Change PIN"),
    ParentalClearPin("Clear"),
    ParentalManageLocked("Manage locked channels…"),
    ParentalSetTitle("Set parental PIN"),
    ParentalPinHint("PIN (4 digits)"),
    ParentalConfirmHint("Confirm PIN"),
    ParentalLockedTitle("🔒 Locked content"),
    ParentalEnterPin("Enter your parental PIN to continue."),
    ParentalWrongPin("Wrong PIN."),
    ParentalUnlock("Unlock"),
    CategoriesManage("Manage categories"),
    CategoriesFilterHint("Filter category names…"),
    CategoriesHideAll("Hide all filtered"),
    CategoriesShowAll("Show all filtered"),
    CategoriesHideAdult("🔞 Hide adult"),
    CategoriesResetAll("Reset everything"),
    CategoriesEmpty("No categories yet — add a provider and re-sync."),
    CategoriesShow("Show"),
    CategoriesHide("Hide"),
    CategoriesCountTemplate("%1\$d total · %2\$d shown · %3\$d hidden"),
    WizardWelcomeTitle("👋 Welcome to Tviito TV"),
    WizardAddProviderTitle("📡 Add a provider"),
    WizardDoneTitle("🎉 You're set"),
    WizardStepTemplate("Step %1\$d / %2\$d"),
    WizardIntro1("Tviito TV is a native Android-TV IPTV client. It speaks Xtream Codes, M3U / M3U8, M3U files from local storage, and Stalker Portal."),
    WizardIntro2("It uses Compose-TV for the UI, Media3 / ExoPlayer for playback, Room for the catalog. D-pad navigation works out of the box."),
    WizardTwoPaths("Two paths:"),
    WizardPathManual("• Settings → +Xtream / +M3U URL / +M3U file / +Stalker. Fill in the form."),
    WizardPathCloud("• Or open your Cloudflare Worker dashboard, paste the MAC below, add your providers there, then Settings → Sync from cloud."),
    WizardTipsHead("Tips you can come back to anytime:"),
    WizardTipDefault("• ★ Default provider switching is in Settings."),
    WizardTipSleep("• 💤 Sleep timer + 📊 Stream stats live in the player overlay."),
    WizardTipLock("• 🔒 Lock individual channels via Settings → Parental."),
    WizardTipBackup("• 💾 Backup & restore exports providers + favorites + history as JSON."),
    WizardTipGuide("• 🗓 Guide → Refresh xmltv pulls a 12 h EPG grid."),
    WizardBack("Back"),
    WizardNext("Next"),
    WizardAddProviderCta("Add a provider →"),
    WizardSkip("Skip for now"),
    LockChannelsTitle("Lock individual channels"),
    LockChannelsSubtitle("Locked channels need the parental PIN to play. %1\$d total · %2\$d locked"),
    LockChannelsFilterHint("Filter channels…"),
    LockChannelsLock("Lock"),
    LockChannelsUnlock("Unlock"),
    GuideClickHint("Click a channel to load its next programmes."),
    GuideLoadingEpg("Loading EPG…"),
    GuideProgrammesTemplate("%1\$d programmes loaded · %2\$d channels"),
    GuideLoading("Loading…"),
    GuideRefreshXmltv("Refresh xmltv"),
    GuideNoChannels("No channels — add a provider in Settings."),
    AddProviderAdd("Add"),
    AddProviderXtreamTitle("Add Xtream Codes provider"),
    AddProviderM3uTitle("Add M3U playlist"),
    AddProviderStalkerTitle("Add Stalker portal"),
    FieldNameOptional("Name (optional)"),
    FieldServerUrl("Server URL (http://host:port)"),
    FieldUsername("Username"),
    FieldPassword("Password"),
    FieldPlaylistUrl("Playlist URL"),
    FieldPortalUrl("Portal URL"),
    FieldDeviceMac("Device MAC (XX:XX:XX:XX:XX:XX)"),
    SettingsAutoImportTitle("📡 Auto-import via device MAC"),
    SettingsYourMac("Your MAC:"),
    SettingsMacHint("Open your worker dashboard, paste this MAC, add your providers there, then press Sync."),
    SettingsConfigPasswordLabel("Config password: "),
    SettingsConfigPasswordNone("(none — anyone with the MAC reads it)"),
    SettingsSet("Set"),
    SettingsSyncing("Working…"),
    SettingsSyncFromCloud("Sync from cloud"),
    SettingsAddProviderTitle("➕ Add a provider"),
    SettingsAddProviderHint("Tap a button to open the form. The IME only shows up inside the dialog — you won't trip on it while scrolling Settings."),
    SettingsAddXtream("+ Xtream Codes"),
    SettingsAddM3uUrl("+ M3U URL"),
    SettingsAddM3uFile("+ M3U file…"),
    SettingsAddStalker("+ Stalker portal"),
    SettingsConfiguredHeader("Configured providers"),
    SettingsNoneYet("(none yet)"),
    SettingsDefaultBadge("★ Default"),
    SettingsSetDefault("Set default"),
    SettingsResync("Re-sync"),
    SettingsBackupTitle("💾 Backup & restore"),
    SettingsBackupHint("Exports providers, favorites and watch history to a JSON file you choose. Catalogs (channels / movies / series) are re-fetched via sync, not bundled."),
    SettingsBackupExport("Export backup"),
    SettingsBackupImport("Import backup…"),
    SettingsParentalHint("When a PIN is set, adult categories (xxx / adult / 18+ / etc.) auto-lock on each sync."),
    SettingsConfigPwdDialogTitle("Config password"),
    SettingsConfigPwdDialogHint("Each MAC's config on the worker can be protected by a password. Set the same value the admin set when provisioning your MAC. Leave blank to send no password (works only for unprotected MACs)."),
    SettingsConfigPwdFieldLabel("Password (visible — TV remote-friendly)"),
    SettingsConfigPwdFieldPlaceholder("Leave blank to clear"),
    SettingsWorkerDialogTitle("Set Cloudflare Worker URL"),
    SettingsWorkerDialogHint("Each user deploys their own Worker (cloudflare-config/) and pastes its URL here. We never bundle a default URL in the app to avoid leaking yours through the source code."),
    SettingsWorkerFieldLabel("Worker base URL (e.g. https://your-config.your-acct.workers.dev)"),
    ToastBackupSaved("Backup saved"),
    ToastSaveFailed("Save failed: "),
    ToastEmptyFile("Empty / unreadable file"),
    ToastConfigPasswordSaved("Config password saved"),
    PrefSidebar("Sidebar"),
    PrefTopBar("Top bar"),
    PrefThemeDark("Dark"),
    PrefThemeAmoled("AMOLED"),
    PrefThemeBlue("Blue"),
    PrefDefaultPlayer("Default player"),
    PrefPlayerInternal("Internal (Media3)"),
    PrefPlayerExternal("External (VLC / MX)"),
    PrefAutoSyncHint("Pull provider catalogs every time the app starts."),
    PrefShowChannelNumbers("Show channel numbers"),
    PrefShowChannelNumbersHint("Display the position number next to each channel in Live TV."),
    PrefHideAdult("Hide adult categories"),
    PrefHideAdultHint("Completely remove adult categories from lists (beyond PIN lock)."),
    PrefResume("Resume playback"),
    PrefResumeHint("Reopen movies/episodes at the position you left them."),
    PrefAutoPlayNext("Auto-play next episode"),
    PrefAutoPlayNextHint("Automatically play S0xE0y+1 when an episode ends."),
    PrefLaunchAtBoot("Launch at TV boot"),
    PrefLaunchAtBootHint("Open Tviito TV automatically when the box finishes booting."),
    PrefAutoPlayLast("Auto-play last watched on launch"),
    PrefAutoPlayLastHint("Resume the last channel / movie / episode when the app starts."),
    PrefIntervalLaunch("Every launch"),
    PrefInterval6("Every 6h"),
    PrefInterval12("Every 12h"),
    PrefInterval24("Every 24h"),
    FavoritesEmpty("Nothing favorited yet — open a movie or series and tap ☆."),
    FavoritesMoviesSection("Movies — %1\$d"),
    FavoritesSeriesSection("Series — %1\$d"),
    DetailLoading("Loading…"),
    SeriesNoEpisodes("No episodes available."),
    MultiViewTitle("Multi-View"),
    MultiViewHint("Tap a tile to assign a channel. ENTER cycles through tiles."),
    MultiViewPickTemplate("Choose a channel for tile %1\$d:"),
    RecordingsPlay("Play"),
    RecordingsOpenWith("Open with…"),
    PlayerOff("Off"),
    PlayerAudioTemplate("Audio (%1\$d)"),
    PlayerSubtitlesTemplate("Subtitles (%1\$d)"),
    LiveAllChannels("All channels"),
    LiveChannelsCountTemplate("%1\$d channels"),
    LiveNoChannelsInCategory("No channels in this category."),
    LiveZappingEyebrow("ZAPPING"),
    LiveOnAirPill("ON AIR"),
    LiveThen("then"),
    LiveDayScheduleEyebrow("TODAY'S SCHEDULE"),
    LiveNoEpgForChannel("No EPG available for this channel."),
    UpdateAvailableEyebrow("UPDATE AVAILABLE"),
    UpdateLater("Later"),
    UpdateInstall("Install now"),
    UpdateDownloading("Downloading…"),
    SettingsTelemetryTitle("Remote diagnostics"),
    SettingsTelemetryHint("Send crash + event logs to the worker for debugging. Off = no outbound telemetry."),
    SettingsCheckForUpdates("Check for updates"),
    SettingsCheckingForUpdates("Checking…"),
    SettingsUpToDateTemplate("You are up to date (v%s)"),
    SettingsUpdateAvailableTemplate("Update %s available"),
    BackupEncryptHint("The exported file contains your Xtream / Stalker credentials in clear text. Set a password to encrypt the backup with AES-GCM (recommended)."),
    BackupEncryptFieldLabel("Encryption password (optional)"),
    BackupEncryptFieldPlaceholder("Leave empty for a plain export"),
    RailOther("Other"),
    Open("Open"),
    SleepLabel("Sleep"),
    SleepMin15("15 min"),
    SleepMin30("30 min"),
    Sleep1h("1 hour"),
    Sleep2h("2 hours"),
    SleepCancel("Cancel timer"),
    SleepReached("Sleep timer reached — playback paused"),
    RecordingQueuedTemplate("Recording queued (max %1\$d min)"),
    StatResolution("Resolution"),
    StatVideoCodec("Video codec"),
    StatFrameRate("Frame rate"),
    StatVideoBitrate("Video bitrate"),
    StatAudioCodec("Audio codec"),
    StatAudioChannels("Audio channels"),
    StatBuffered("Buffered"),
    StatDroppedFrames("Dropped frames"),
    ToastBackupReady("Backup ready — pick a file to save it."),
    ToastRestoredTemplate("Restored %1\$d provider(s), %2\$d fav, %3\$d watch entries"),
    ToastRestoreFailed("Restore failed: "),
    ToastRecordingQueued("Recording queued — see Recordings screen"),
    SettingsTitle("Settings"),
    SettingsDisplay("Display & playback"),
    SettingsParental("Parental controls"),
    SettingsBackup("Backup & restore"),
    SettingsLanguage("Language"),
    SettingsTheme("Theme"),
    SettingsMenuPosition("Menu position"),
    SettingsAutoSync("Auto-sync on launch"),
    SettingsRefreshPlaylists("Refresh playlists"),
    MovieDetailPlot("Plot"),
    SeriesDetailEpisodes("Episodes"),
    MoviesTitle("Movies"),
    SeriesTitle("Series"),
    NoMovies("No movies — add a provider in Settings and re-sync."),
    NoSeries("No series — add a provider in Settings and re-sync."),
    PlayerSleep("Sleep"),
    PlayerStats("Stats"),
    PlayerTracks("Tracks"),
    PlayerDisplay("Display"),
    PlayerExternal("External player"),
    PlayerCast("Cast"),
    PlayerRecord("Record"),
    PlayerAspect("Aspect"),
    PlayerSpeed("Speed"),
    PlayerZapHint("▲ ▼ to zap channels"),
    SearchPlaceholder("Type to search channels, movies, series…"),
    SearchRecent("Recent:"),
    SearchClear("Clear"),
    SearchNoMatches("No matches."),
    RecordingsTitle("Recordings"),
    RecordingsEmpty("No recordings yet. Open a movie or episode and press the ⏺ Record button to queue a download."),
    RecordingStatusQueued("Queued"),
    RecordingStatusRunning("Downloading…"),
    RecordingStatusDone("Saved"),
    RecordingStatusFailed("Failed"),
    RecordingStatusCancelled("Cancelled"),
    Live("Live TV"),
    Movies("Movies"),
    Series("Series"),
    Categories("Categories"),
    TvGuide("TV Guide"),
    Favorites("Favorites"),
    Play("Play"),
    Resume("Resume"),
    Cancel("Cancel"),
    Close("Close"),
    Save("Save"),
    Delete("Delete"),
    Confirm("Confirm"),
    Dismiss("Dismiss"),
    Change("Change");
}

class Strings(
    private val overrides: Map<StringKey, String> = emptyMap(),
) {
    val navHome: String get() = overrides[StringKey.NavHome] ?: StringKey.NavHome.defaultValue
    val navLive: String get() = overrides[StringKey.NavLive] ?: StringKey.NavLive.defaultValue
    val navGuide: String get() = overrides[StringKey.NavGuide] ?: StringKey.NavGuide.defaultValue
    val navMovies: String get() = overrides[StringKey.NavMovies] ?: StringKey.NavMovies.defaultValue
    val navSeries: String get() = overrides[StringKey.NavSeries] ?: StringKey.NavSeries.defaultValue
    val navFavorites: String get() = overrides[StringKey.NavFavorites] ?: StringKey.NavFavorites.defaultValue
    val navSearch: String get() = overrides[StringKey.NavSearch] ?: StringKey.NavSearch.defaultValue
    val navCategories: String get() = overrides[StringKey.NavCategories] ?: StringKey.NavCategories.defaultValue
    val navMultiview: String get() = overrides[StringKey.NavMultiview] ?: StringKey.NavMultiview.defaultValue
    val navRecordings: String get() = overrides[StringKey.NavRecordings] ?: StringKey.NavRecordings.defaultValue
    val navSettings: String get() = overrides[StringKey.NavSettings] ?: StringKey.NavSettings.defaultValue
    val homeWelcome: String get() = overrides[StringKey.HomeWelcome] ?: StringKey.HomeWelcome.defaultValue
    val homeSubtitle: String get() = overrides[StringKey.HomeSubtitle] ?: StringKey.HomeSubtitle.defaultValue
    val homeContinueWatching: String get() = overrides[StringKey.HomeContinueWatching] ?: StringKey.HomeContinueWatching.defaultValue
    val homeRecentlyWatched: String get() = overrides[StringKey.HomeRecentlyWatched] ?: StringKey.HomeRecentlyWatched.defaultValue
    val homeFeaturedChannels: String get() = overrides[StringKey.HomeFeaturedChannels] ?: StringKey.HomeFeaturedChannels.defaultValue
    val homeFeaturedMovies: String get() = overrides[StringKey.HomeFeaturedMovies] ?: StringKey.HomeFeaturedMovies.defaultValue
    val homeFeaturedSeries: String get() = overrides[StringKey.HomeFeaturedSeries] ?: StringKey.HomeFeaturedSeries.defaultValue
    val onboardingMacLabel: String get() = overrides[StringKey.OnboardingMacLabel] ?: StringKey.OnboardingMacLabel.defaultValue
    val onboardingOpenSettings: String get() = overrides[StringKey.OnboardingOpenSettings] ?: StringKey.OnboardingOpenSettings.defaultValue
    val onboardingFirstTime: String get() = overrides[StringKey.OnboardingFirstTime] ?: StringKey.OnboardingFirstTime.defaultValue
    val onboardingTwoPaths: String get() = overrides[StringKey.OnboardingTwoPaths] ?: StringKey.OnboardingTwoPaths.defaultValue
    val onboardingPathManual: String get() = overrides[StringKey.OnboardingPathManual] ?: StringKey.OnboardingPathManual.defaultValue
    val onboardingPathCloud: String get() = overrides[StringKey.OnboardingPathCloud] ?: StringKey.OnboardingPathCloud.defaultValue
    val parentalPinEnabled: String get() = overrides[StringKey.ParentalPinEnabled] ?: StringKey.ParentalPinEnabled.defaultValue
    val parentalPinNotSet: String get() = overrides[StringKey.ParentalPinNotSet] ?: StringKey.ParentalPinNotSet.defaultValue
    val parentalSetPin: String get() = overrides[StringKey.ParentalSetPin] ?: StringKey.ParentalSetPin.defaultValue
    val parentalChangePin: String get() = overrides[StringKey.ParentalChangePin] ?: StringKey.ParentalChangePin.defaultValue
    val parentalClearPin: String get() = overrides[StringKey.ParentalClearPin] ?: StringKey.ParentalClearPin.defaultValue
    val parentalManageLocked: String get() = overrides[StringKey.ParentalManageLocked] ?: StringKey.ParentalManageLocked.defaultValue
    val parentalSetTitle: String get() = overrides[StringKey.ParentalSetTitle] ?: StringKey.ParentalSetTitle.defaultValue
    val parentalPinHint: String get() = overrides[StringKey.ParentalPinHint] ?: StringKey.ParentalPinHint.defaultValue
    val parentalConfirmHint: String get() = overrides[StringKey.ParentalConfirmHint] ?: StringKey.ParentalConfirmHint.defaultValue
    val parentalLockedTitle: String get() = overrides[StringKey.ParentalLockedTitle] ?: StringKey.ParentalLockedTitle.defaultValue
    val parentalEnterPin: String get() = overrides[StringKey.ParentalEnterPin] ?: StringKey.ParentalEnterPin.defaultValue
    val parentalWrongPin: String get() = overrides[StringKey.ParentalWrongPin] ?: StringKey.ParentalWrongPin.defaultValue
    val parentalUnlock: String get() = overrides[StringKey.ParentalUnlock] ?: StringKey.ParentalUnlock.defaultValue
    val categoriesManage: String get() = overrides[StringKey.CategoriesManage] ?: StringKey.CategoriesManage.defaultValue
    val categoriesFilterHint: String get() = overrides[StringKey.CategoriesFilterHint] ?: StringKey.CategoriesFilterHint.defaultValue
    val categoriesHideAll: String get() = overrides[StringKey.CategoriesHideAll] ?: StringKey.CategoriesHideAll.defaultValue
    val categoriesShowAll: String get() = overrides[StringKey.CategoriesShowAll] ?: StringKey.CategoriesShowAll.defaultValue
    val categoriesHideAdult: String get() = overrides[StringKey.CategoriesHideAdult] ?: StringKey.CategoriesHideAdult.defaultValue
    val categoriesResetAll: String get() = overrides[StringKey.CategoriesResetAll] ?: StringKey.CategoriesResetAll.defaultValue
    val categoriesEmpty: String get() = overrides[StringKey.CategoriesEmpty] ?: StringKey.CategoriesEmpty.defaultValue
    val categoriesShow: String get() = overrides[StringKey.CategoriesShow] ?: StringKey.CategoriesShow.defaultValue
    val categoriesHide: String get() = overrides[StringKey.CategoriesHide] ?: StringKey.CategoriesHide.defaultValue
    val categoriesCountTemplate: String get() = overrides[StringKey.CategoriesCountTemplate] ?: StringKey.CategoriesCountTemplate.defaultValue
    val wizardWelcomeTitle: String get() = overrides[StringKey.WizardWelcomeTitle] ?: StringKey.WizardWelcomeTitle.defaultValue
    val wizardAddProviderTitle: String get() = overrides[StringKey.WizardAddProviderTitle] ?: StringKey.WizardAddProviderTitle.defaultValue
    val wizardDoneTitle: String get() = overrides[StringKey.WizardDoneTitle] ?: StringKey.WizardDoneTitle.defaultValue
    val wizardStepTemplate: String get() = overrides[StringKey.WizardStepTemplate] ?: StringKey.WizardStepTemplate.defaultValue
    val wizardIntro1: String get() = overrides[StringKey.WizardIntro1] ?: StringKey.WizardIntro1.defaultValue
    val wizardIntro2: String get() = overrides[StringKey.WizardIntro2] ?: StringKey.WizardIntro2.defaultValue
    val wizardTwoPaths: String get() = overrides[StringKey.WizardTwoPaths] ?: StringKey.WizardTwoPaths.defaultValue
    val wizardPathManual: String get() = overrides[StringKey.WizardPathManual] ?: StringKey.WizardPathManual.defaultValue
    val wizardPathCloud: String get() = overrides[StringKey.WizardPathCloud] ?: StringKey.WizardPathCloud.defaultValue
    val wizardTipsHead: String get() = overrides[StringKey.WizardTipsHead] ?: StringKey.WizardTipsHead.defaultValue
    val wizardTipDefault: String get() = overrides[StringKey.WizardTipDefault] ?: StringKey.WizardTipDefault.defaultValue
    val wizardTipSleep: String get() = overrides[StringKey.WizardTipSleep] ?: StringKey.WizardTipSleep.defaultValue
    val wizardTipLock: String get() = overrides[StringKey.WizardTipLock] ?: StringKey.WizardTipLock.defaultValue
    val wizardTipBackup: String get() = overrides[StringKey.WizardTipBackup] ?: StringKey.WizardTipBackup.defaultValue
    val wizardTipGuide: String get() = overrides[StringKey.WizardTipGuide] ?: StringKey.WizardTipGuide.defaultValue
    val wizardBack: String get() = overrides[StringKey.WizardBack] ?: StringKey.WizardBack.defaultValue
    val wizardNext: String get() = overrides[StringKey.WizardNext] ?: StringKey.WizardNext.defaultValue
    val wizardAddProviderCta: String get() = overrides[StringKey.WizardAddProviderCta] ?: StringKey.WizardAddProviderCta.defaultValue
    val wizardSkip: String get() = overrides[StringKey.WizardSkip] ?: StringKey.WizardSkip.defaultValue
    val lockChannelsTitle: String get() = overrides[StringKey.LockChannelsTitle] ?: StringKey.LockChannelsTitle.defaultValue
    val lockChannelsSubtitle: String get() = overrides[StringKey.LockChannelsSubtitle] ?: StringKey.LockChannelsSubtitle.defaultValue
    val lockChannelsFilterHint: String get() = overrides[StringKey.LockChannelsFilterHint] ?: StringKey.LockChannelsFilterHint.defaultValue
    val lockChannelsLock: String get() = overrides[StringKey.LockChannelsLock] ?: StringKey.LockChannelsLock.defaultValue
    val lockChannelsUnlock: String get() = overrides[StringKey.LockChannelsUnlock] ?: StringKey.LockChannelsUnlock.defaultValue
    val guideClickHint: String get() = overrides[StringKey.GuideClickHint] ?: StringKey.GuideClickHint.defaultValue
    val guideLoadingEpg: String get() = overrides[StringKey.GuideLoadingEpg] ?: StringKey.GuideLoadingEpg.defaultValue
    val guideProgrammesTemplate: String get() = overrides[StringKey.GuideProgrammesTemplate] ?: StringKey.GuideProgrammesTemplate.defaultValue
    val guideLoading: String get() = overrides[StringKey.GuideLoading] ?: StringKey.GuideLoading.defaultValue
    val guideRefreshXmltv: String get() = overrides[StringKey.GuideRefreshXmltv] ?: StringKey.GuideRefreshXmltv.defaultValue
    val guideNoChannels: String get() = overrides[StringKey.GuideNoChannels] ?: StringKey.GuideNoChannels.defaultValue
    val addProviderAdd: String get() = overrides[StringKey.AddProviderAdd] ?: StringKey.AddProviderAdd.defaultValue
    val addProviderXtreamTitle: String get() = overrides[StringKey.AddProviderXtreamTitle] ?: StringKey.AddProviderXtreamTitle.defaultValue
    val addProviderM3uTitle: String get() = overrides[StringKey.AddProviderM3uTitle] ?: StringKey.AddProviderM3uTitle.defaultValue
    val addProviderStalkerTitle: String get() = overrides[StringKey.AddProviderStalkerTitle] ?: StringKey.AddProviderStalkerTitle.defaultValue
    val fieldNameOptional: String get() = overrides[StringKey.FieldNameOptional] ?: StringKey.FieldNameOptional.defaultValue
    val fieldServerUrl: String get() = overrides[StringKey.FieldServerUrl] ?: StringKey.FieldServerUrl.defaultValue
    val fieldUsername: String get() = overrides[StringKey.FieldUsername] ?: StringKey.FieldUsername.defaultValue
    val fieldPassword: String get() = overrides[StringKey.FieldPassword] ?: StringKey.FieldPassword.defaultValue
    val fieldPlaylistUrl: String get() = overrides[StringKey.FieldPlaylistUrl] ?: StringKey.FieldPlaylistUrl.defaultValue
    val fieldPortalUrl: String get() = overrides[StringKey.FieldPortalUrl] ?: StringKey.FieldPortalUrl.defaultValue
    val fieldDeviceMac: String get() = overrides[StringKey.FieldDeviceMac] ?: StringKey.FieldDeviceMac.defaultValue
    val settingsAutoImportTitle: String get() = overrides[StringKey.SettingsAutoImportTitle] ?: StringKey.SettingsAutoImportTitle.defaultValue
    val settingsYourMac: String get() = overrides[StringKey.SettingsYourMac] ?: StringKey.SettingsYourMac.defaultValue
    val settingsMacHint: String get() = overrides[StringKey.SettingsMacHint] ?: StringKey.SettingsMacHint.defaultValue
    val settingsConfigPasswordLabel: String get() = overrides[StringKey.SettingsConfigPasswordLabel] ?: StringKey.SettingsConfigPasswordLabel.defaultValue
    val settingsConfigPasswordNone: String get() = overrides[StringKey.SettingsConfigPasswordNone] ?: StringKey.SettingsConfigPasswordNone.defaultValue
    val settingsSet: String get() = overrides[StringKey.SettingsSet] ?: StringKey.SettingsSet.defaultValue
    val settingsSyncing: String get() = overrides[StringKey.SettingsSyncing] ?: StringKey.SettingsSyncing.defaultValue
    val settingsSyncFromCloud: String get() = overrides[StringKey.SettingsSyncFromCloud] ?: StringKey.SettingsSyncFromCloud.defaultValue
    val settingsAddProviderTitle: String get() = overrides[StringKey.SettingsAddProviderTitle] ?: StringKey.SettingsAddProviderTitle.defaultValue
    val settingsAddProviderHint: String get() = overrides[StringKey.SettingsAddProviderHint] ?: StringKey.SettingsAddProviderHint.defaultValue
    val settingsAddXtream: String get() = overrides[StringKey.SettingsAddXtream] ?: StringKey.SettingsAddXtream.defaultValue
    val settingsAddM3uUrl: String get() = overrides[StringKey.SettingsAddM3uUrl] ?: StringKey.SettingsAddM3uUrl.defaultValue
    val settingsAddM3uFile: String get() = overrides[StringKey.SettingsAddM3uFile] ?: StringKey.SettingsAddM3uFile.defaultValue
    val settingsAddStalker: String get() = overrides[StringKey.SettingsAddStalker] ?: StringKey.SettingsAddStalker.defaultValue
    val settingsConfiguredHeader: String get() = overrides[StringKey.SettingsConfiguredHeader] ?: StringKey.SettingsConfiguredHeader.defaultValue
    val settingsNoneYet: String get() = overrides[StringKey.SettingsNoneYet] ?: StringKey.SettingsNoneYet.defaultValue
    val settingsDefaultBadge: String get() = overrides[StringKey.SettingsDefaultBadge] ?: StringKey.SettingsDefaultBadge.defaultValue
    val settingsSetDefault: String get() = overrides[StringKey.SettingsSetDefault] ?: StringKey.SettingsSetDefault.defaultValue
    val settingsResync: String get() = overrides[StringKey.SettingsResync] ?: StringKey.SettingsResync.defaultValue
    val settingsBackupTitle: String get() = overrides[StringKey.SettingsBackupTitle] ?: StringKey.SettingsBackupTitle.defaultValue
    val settingsBackupHint: String get() = overrides[StringKey.SettingsBackupHint] ?: StringKey.SettingsBackupHint.defaultValue
    val settingsBackupExport: String get() = overrides[StringKey.SettingsBackupExport] ?: StringKey.SettingsBackupExport.defaultValue
    val settingsBackupImport: String get() = overrides[StringKey.SettingsBackupImport] ?: StringKey.SettingsBackupImport.defaultValue
    val settingsParentalHint: String get() = overrides[StringKey.SettingsParentalHint] ?: StringKey.SettingsParentalHint.defaultValue
    val settingsConfigPwdDialogTitle: String get() = overrides[StringKey.SettingsConfigPwdDialogTitle] ?: StringKey.SettingsConfigPwdDialogTitle.defaultValue
    val settingsConfigPwdDialogHint: String get() = overrides[StringKey.SettingsConfigPwdDialogHint] ?: StringKey.SettingsConfigPwdDialogHint.defaultValue
    val settingsConfigPwdFieldLabel: String get() = overrides[StringKey.SettingsConfigPwdFieldLabel] ?: StringKey.SettingsConfigPwdFieldLabel.defaultValue
    val settingsConfigPwdFieldPlaceholder: String get() = overrides[StringKey.SettingsConfigPwdFieldPlaceholder] ?: StringKey.SettingsConfigPwdFieldPlaceholder.defaultValue
    val settingsWorkerDialogTitle: String get() = overrides[StringKey.SettingsWorkerDialogTitle] ?: StringKey.SettingsWorkerDialogTitle.defaultValue
    val settingsWorkerDialogHint: String get() = overrides[StringKey.SettingsWorkerDialogHint] ?: StringKey.SettingsWorkerDialogHint.defaultValue
    val settingsWorkerFieldLabel: String get() = overrides[StringKey.SettingsWorkerFieldLabel] ?: StringKey.SettingsWorkerFieldLabel.defaultValue
    val toastBackupSaved: String get() = overrides[StringKey.ToastBackupSaved] ?: StringKey.ToastBackupSaved.defaultValue
    val toastSaveFailed: String get() = overrides[StringKey.ToastSaveFailed] ?: StringKey.ToastSaveFailed.defaultValue
    val toastEmptyFile: String get() = overrides[StringKey.ToastEmptyFile] ?: StringKey.ToastEmptyFile.defaultValue
    val toastConfigPasswordSaved: String get() = overrides[StringKey.ToastConfigPasswordSaved] ?: StringKey.ToastConfigPasswordSaved.defaultValue
    val prefSidebar: String get() = overrides[StringKey.PrefSidebar] ?: StringKey.PrefSidebar.defaultValue
    val prefTopBar: String get() = overrides[StringKey.PrefTopBar] ?: StringKey.PrefTopBar.defaultValue
    val prefThemeDark: String get() = overrides[StringKey.PrefThemeDark] ?: StringKey.PrefThemeDark.defaultValue
    val prefThemeAmoled: String get() = overrides[StringKey.PrefThemeAmoled] ?: StringKey.PrefThemeAmoled.defaultValue
    val prefThemeBlue: String get() = overrides[StringKey.PrefThemeBlue] ?: StringKey.PrefThemeBlue.defaultValue
    val prefDefaultPlayer: String get() = overrides[StringKey.PrefDefaultPlayer] ?: StringKey.PrefDefaultPlayer.defaultValue
    val prefPlayerInternal: String get() = overrides[StringKey.PrefPlayerInternal] ?: StringKey.PrefPlayerInternal.defaultValue
    val prefPlayerExternal: String get() = overrides[StringKey.PrefPlayerExternal] ?: StringKey.PrefPlayerExternal.defaultValue
    val prefAutoSyncHint: String get() = overrides[StringKey.PrefAutoSyncHint] ?: StringKey.PrefAutoSyncHint.defaultValue
    val prefShowChannelNumbers: String get() = overrides[StringKey.PrefShowChannelNumbers] ?: StringKey.PrefShowChannelNumbers.defaultValue
    val prefShowChannelNumbersHint: String get() = overrides[StringKey.PrefShowChannelNumbersHint] ?: StringKey.PrefShowChannelNumbersHint.defaultValue
    val prefHideAdult: String get() = overrides[StringKey.PrefHideAdult] ?: StringKey.PrefHideAdult.defaultValue
    val prefHideAdultHint: String get() = overrides[StringKey.PrefHideAdultHint] ?: StringKey.PrefHideAdultHint.defaultValue
    val prefResume: String get() = overrides[StringKey.PrefResume] ?: StringKey.PrefResume.defaultValue
    val prefResumeHint: String get() = overrides[StringKey.PrefResumeHint] ?: StringKey.PrefResumeHint.defaultValue
    val prefAutoPlayNext: String get() = overrides[StringKey.PrefAutoPlayNext] ?: StringKey.PrefAutoPlayNext.defaultValue
    val prefAutoPlayNextHint: String get() = overrides[StringKey.PrefAutoPlayNextHint] ?: StringKey.PrefAutoPlayNextHint.defaultValue
    val prefLaunchAtBoot: String get() = overrides[StringKey.PrefLaunchAtBoot] ?: StringKey.PrefLaunchAtBoot.defaultValue
    val prefLaunchAtBootHint: String get() = overrides[StringKey.PrefLaunchAtBootHint] ?: StringKey.PrefLaunchAtBootHint.defaultValue
    val prefAutoPlayLast: String get() = overrides[StringKey.PrefAutoPlayLast] ?: StringKey.PrefAutoPlayLast.defaultValue
    val prefAutoPlayLastHint: String get() = overrides[StringKey.PrefAutoPlayLastHint] ?: StringKey.PrefAutoPlayLastHint.defaultValue
    val prefIntervalLaunch: String get() = overrides[StringKey.PrefIntervalLaunch] ?: StringKey.PrefIntervalLaunch.defaultValue
    val prefInterval6: String get() = overrides[StringKey.PrefInterval6] ?: StringKey.PrefInterval6.defaultValue
    val prefInterval12: String get() = overrides[StringKey.PrefInterval12] ?: StringKey.PrefInterval12.defaultValue
    val prefInterval24: String get() = overrides[StringKey.PrefInterval24] ?: StringKey.PrefInterval24.defaultValue
    val favoritesEmpty: String get() = overrides[StringKey.FavoritesEmpty] ?: StringKey.FavoritesEmpty.defaultValue
    val favoritesMoviesSection: String get() = overrides[StringKey.FavoritesMoviesSection] ?: StringKey.FavoritesMoviesSection.defaultValue
    val favoritesSeriesSection: String get() = overrides[StringKey.FavoritesSeriesSection] ?: StringKey.FavoritesSeriesSection.defaultValue
    val detailLoading: String get() = overrides[StringKey.DetailLoading] ?: StringKey.DetailLoading.defaultValue
    val seriesNoEpisodes: String get() = overrides[StringKey.SeriesNoEpisodes] ?: StringKey.SeriesNoEpisodes.defaultValue
    val multiViewTitle: String get() = overrides[StringKey.MultiViewTitle] ?: StringKey.MultiViewTitle.defaultValue
    val multiViewHint: String get() = overrides[StringKey.MultiViewHint] ?: StringKey.MultiViewHint.defaultValue
    val multiViewPickTemplate: String get() = overrides[StringKey.MultiViewPickTemplate] ?: StringKey.MultiViewPickTemplate.defaultValue
    val recordingsPlay: String get() = overrides[StringKey.RecordingsPlay] ?: StringKey.RecordingsPlay.defaultValue
    val recordingsOpenWith: String get() = overrides[StringKey.RecordingsOpenWith] ?: StringKey.RecordingsOpenWith.defaultValue
    val playerOff: String get() = overrides[StringKey.PlayerOff] ?: StringKey.PlayerOff.defaultValue
    val playerAudioTemplate: String get() = overrides[StringKey.PlayerAudioTemplate] ?: StringKey.PlayerAudioTemplate.defaultValue
    val playerSubtitlesTemplate: String get() = overrides[StringKey.PlayerSubtitlesTemplate] ?: StringKey.PlayerSubtitlesTemplate.defaultValue
    val liveAllChannels: String get() = overrides[StringKey.LiveAllChannels] ?: StringKey.LiveAllChannels.defaultValue
    val liveChannelsCountTemplate: String get() = overrides[StringKey.LiveChannelsCountTemplate] ?: StringKey.LiveChannelsCountTemplate.defaultValue
    val liveNoChannelsInCategory: String get() = overrides[StringKey.LiveNoChannelsInCategory] ?: StringKey.LiveNoChannelsInCategory.defaultValue
    val liveZappingEyebrow: String get() = overrides[StringKey.LiveZappingEyebrow] ?: StringKey.LiveZappingEyebrow.defaultValue
    val liveOnAirPill: String get() = overrides[StringKey.LiveOnAirPill] ?: StringKey.LiveOnAirPill.defaultValue
    val liveThen: String get() = overrides[StringKey.LiveThen] ?: StringKey.LiveThen.defaultValue
    val liveDayScheduleEyebrow: String get() = overrides[StringKey.LiveDayScheduleEyebrow] ?: StringKey.LiveDayScheduleEyebrow.defaultValue
    val liveNoEpgForChannel: String get() = overrides[StringKey.LiveNoEpgForChannel] ?: StringKey.LiveNoEpgForChannel.defaultValue
    val updateAvailableEyebrow: String get() = overrides[StringKey.UpdateAvailableEyebrow] ?: StringKey.UpdateAvailableEyebrow.defaultValue
    val updateLater: String get() = overrides[StringKey.UpdateLater] ?: StringKey.UpdateLater.defaultValue
    val updateInstall: String get() = overrides[StringKey.UpdateInstall] ?: StringKey.UpdateInstall.defaultValue
    val updateDownloading: String get() = overrides[StringKey.UpdateDownloading] ?: StringKey.UpdateDownloading.defaultValue
    val settingsTelemetryTitle: String get() = overrides[StringKey.SettingsTelemetryTitle] ?: StringKey.SettingsTelemetryTitle.defaultValue
    val settingsTelemetryHint: String get() = overrides[StringKey.SettingsTelemetryHint] ?: StringKey.SettingsTelemetryHint.defaultValue
    val settingsCheckForUpdates: String get() = overrides[StringKey.SettingsCheckForUpdates] ?: StringKey.SettingsCheckForUpdates.defaultValue
    val settingsCheckingForUpdates: String get() = overrides[StringKey.SettingsCheckingForUpdates] ?: StringKey.SettingsCheckingForUpdates.defaultValue
    val settingsUpToDateTemplate: String get() = overrides[StringKey.SettingsUpToDateTemplate] ?: StringKey.SettingsUpToDateTemplate.defaultValue
    val settingsUpdateAvailableTemplate: String get() = overrides[StringKey.SettingsUpdateAvailableTemplate] ?: StringKey.SettingsUpdateAvailableTemplate.defaultValue
    val backupEncryptHint: String get() = overrides[StringKey.BackupEncryptHint] ?: StringKey.BackupEncryptHint.defaultValue
    val backupEncryptFieldLabel: String get() = overrides[StringKey.BackupEncryptFieldLabel] ?: StringKey.BackupEncryptFieldLabel.defaultValue
    val backupEncryptFieldPlaceholder: String get() = overrides[StringKey.BackupEncryptFieldPlaceholder] ?: StringKey.BackupEncryptFieldPlaceholder.defaultValue
    val railOther: String get() = overrides[StringKey.RailOther] ?: StringKey.RailOther.defaultValue
    val open: String get() = overrides[StringKey.Open] ?: StringKey.Open.defaultValue
    val sleepLabel: String get() = overrides[StringKey.SleepLabel] ?: StringKey.SleepLabel.defaultValue
    val sleepMin15: String get() = overrides[StringKey.SleepMin15] ?: StringKey.SleepMin15.defaultValue
    val sleepMin30: String get() = overrides[StringKey.SleepMin30] ?: StringKey.SleepMin30.defaultValue
    val sleep1h: String get() = overrides[StringKey.Sleep1h] ?: StringKey.Sleep1h.defaultValue
    val sleep2h: String get() = overrides[StringKey.Sleep2h] ?: StringKey.Sleep2h.defaultValue
    val sleepCancel: String get() = overrides[StringKey.SleepCancel] ?: StringKey.SleepCancel.defaultValue
    val sleepReached: String get() = overrides[StringKey.SleepReached] ?: StringKey.SleepReached.defaultValue
    val recordingQueuedTemplate: String get() = overrides[StringKey.RecordingQueuedTemplate] ?: StringKey.RecordingQueuedTemplate.defaultValue
    val statResolution: String get() = overrides[StringKey.StatResolution] ?: StringKey.StatResolution.defaultValue
    val statVideoCodec: String get() = overrides[StringKey.StatVideoCodec] ?: StringKey.StatVideoCodec.defaultValue
    val statFrameRate: String get() = overrides[StringKey.StatFrameRate] ?: StringKey.StatFrameRate.defaultValue
    val statVideoBitrate: String get() = overrides[StringKey.StatVideoBitrate] ?: StringKey.StatVideoBitrate.defaultValue
    val statAudioCodec: String get() = overrides[StringKey.StatAudioCodec] ?: StringKey.StatAudioCodec.defaultValue
    val statAudioChannels: String get() = overrides[StringKey.StatAudioChannels] ?: StringKey.StatAudioChannels.defaultValue
    val statBuffered: String get() = overrides[StringKey.StatBuffered] ?: StringKey.StatBuffered.defaultValue
    val statDroppedFrames: String get() = overrides[StringKey.StatDroppedFrames] ?: StringKey.StatDroppedFrames.defaultValue
    val toastBackupReady: String get() = overrides[StringKey.ToastBackupReady] ?: StringKey.ToastBackupReady.defaultValue
    val toastRestoredTemplate: String get() = overrides[StringKey.ToastRestoredTemplate] ?: StringKey.ToastRestoredTemplate.defaultValue
    val toastRestoreFailed: String get() = overrides[StringKey.ToastRestoreFailed] ?: StringKey.ToastRestoreFailed.defaultValue
    val toastRecordingQueued: String get() = overrides[StringKey.ToastRecordingQueued] ?: StringKey.ToastRecordingQueued.defaultValue
    val settingsTitle: String get() = overrides[StringKey.SettingsTitle] ?: StringKey.SettingsTitle.defaultValue
    val settingsDisplay: String get() = overrides[StringKey.SettingsDisplay] ?: StringKey.SettingsDisplay.defaultValue
    val settingsParental: String get() = overrides[StringKey.SettingsParental] ?: StringKey.SettingsParental.defaultValue
    val settingsBackup: String get() = overrides[StringKey.SettingsBackup] ?: StringKey.SettingsBackup.defaultValue
    val settingsLanguage: String get() = overrides[StringKey.SettingsLanguage] ?: StringKey.SettingsLanguage.defaultValue
    val settingsTheme: String get() = overrides[StringKey.SettingsTheme] ?: StringKey.SettingsTheme.defaultValue
    val settingsMenuPosition: String get() = overrides[StringKey.SettingsMenuPosition] ?: StringKey.SettingsMenuPosition.defaultValue
    val settingsAutoSync: String get() = overrides[StringKey.SettingsAutoSync] ?: StringKey.SettingsAutoSync.defaultValue
    val settingsRefreshPlaylists: String get() = overrides[StringKey.SettingsRefreshPlaylists] ?: StringKey.SettingsRefreshPlaylists.defaultValue
    val movieDetailPlot: String get() = overrides[StringKey.MovieDetailPlot] ?: StringKey.MovieDetailPlot.defaultValue
    val seriesDetailEpisodes: String get() = overrides[StringKey.SeriesDetailEpisodes] ?: StringKey.SeriesDetailEpisodes.defaultValue
    val moviesTitle: String get() = overrides[StringKey.MoviesTitle] ?: StringKey.MoviesTitle.defaultValue
    val seriesTitle: String get() = overrides[StringKey.SeriesTitle] ?: StringKey.SeriesTitle.defaultValue
    val noMovies: String get() = overrides[StringKey.NoMovies] ?: StringKey.NoMovies.defaultValue
    val noSeries: String get() = overrides[StringKey.NoSeries] ?: StringKey.NoSeries.defaultValue
    val playerSleep: String get() = overrides[StringKey.PlayerSleep] ?: StringKey.PlayerSleep.defaultValue
    val playerStats: String get() = overrides[StringKey.PlayerStats] ?: StringKey.PlayerStats.defaultValue
    val playerTracks: String get() = overrides[StringKey.PlayerTracks] ?: StringKey.PlayerTracks.defaultValue
    val playerDisplay: String get() = overrides[StringKey.PlayerDisplay] ?: StringKey.PlayerDisplay.defaultValue
    val playerExternal: String get() = overrides[StringKey.PlayerExternal] ?: StringKey.PlayerExternal.defaultValue
    val playerCast: String get() = overrides[StringKey.PlayerCast] ?: StringKey.PlayerCast.defaultValue
    val playerRecord: String get() = overrides[StringKey.PlayerRecord] ?: StringKey.PlayerRecord.defaultValue
    val playerAspect: String get() = overrides[StringKey.PlayerAspect] ?: StringKey.PlayerAspect.defaultValue
    val playerSpeed: String get() = overrides[StringKey.PlayerSpeed] ?: StringKey.PlayerSpeed.defaultValue
    val playerZapHint: String get() = overrides[StringKey.PlayerZapHint] ?: StringKey.PlayerZapHint.defaultValue
    val searchPlaceholder: String get() = overrides[StringKey.SearchPlaceholder] ?: StringKey.SearchPlaceholder.defaultValue
    val searchRecent: String get() = overrides[StringKey.SearchRecent] ?: StringKey.SearchRecent.defaultValue
    val searchClear: String get() = overrides[StringKey.SearchClear] ?: StringKey.SearchClear.defaultValue
    val searchNoMatches: String get() = overrides[StringKey.SearchNoMatches] ?: StringKey.SearchNoMatches.defaultValue
    val recordingsTitle: String get() = overrides[StringKey.RecordingsTitle] ?: StringKey.RecordingsTitle.defaultValue
    val recordingsEmpty: String get() = overrides[StringKey.RecordingsEmpty] ?: StringKey.RecordingsEmpty.defaultValue
    val recordingStatusQueued: String get() = overrides[StringKey.RecordingStatusQueued] ?: StringKey.RecordingStatusQueued.defaultValue
    val recordingStatusRunning: String get() = overrides[StringKey.RecordingStatusRunning] ?: StringKey.RecordingStatusRunning.defaultValue
    val recordingStatusDone: String get() = overrides[StringKey.RecordingStatusDone] ?: StringKey.RecordingStatusDone.defaultValue
    val recordingStatusFailed: String get() = overrides[StringKey.RecordingStatusFailed] ?: StringKey.RecordingStatusFailed.defaultValue
    val recordingStatusCancelled: String get() = overrides[StringKey.RecordingStatusCancelled] ?: StringKey.RecordingStatusCancelled.defaultValue
    val live: String get() = overrides[StringKey.Live] ?: StringKey.Live.defaultValue
    val movies: String get() = overrides[StringKey.Movies] ?: StringKey.Movies.defaultValue
    val series: String get() = overrides[StringKey.Series] ?: StringKey.Series.defaultValue
    val categories: String get() = overrides[StringKey.Categories] ?: StringKey.Categories.defaultValue
    val tvGuide: String get() = overrides[StringKey.TvGuide] ?: StringKey.TvGuide.defaultValue
    val favorites: String get() = overrides[StringKey.Favorites] ?: StringKey.Favorites.defaultValue
    val play: String get() = overrides[StringKey.Play] ?: StringKey.Play.defaultValue
    val resume: String get() = overrides[StringKey.Resume] ?: StringKey.Resume.defaultValue
    val cancel: String get() = overrides[StringKey.Cancel] ?: StringKey.Cancel.defaultValue
    val close: String get() = overrides[StringKey.Close] ?: StringKey.Close.defaultValue
    val save: String get() = overrides[StringKey.Save] ?: StringKey.Save.defaultValue
    val delete: String get() = overrides[StringKey.Delete] ?: StringKey.Delete.defaultValue
    val confirm: String get() = overrides[StringKey.Confirm] ?: StringKey.Confirm.defaultValue
    val dismiss: String get() = overrides[StringKey.Dismiss] ?: StringKey.Dismiss.defaultValue
    val change: String get() = overrides[StringKey.Change] ?: StringKey.Change.defaultValue
}


private fun stringsOf(configure: MutableMap<StringKey, String>.() -> Unit): Strings {
    val overrides = EnumMap<StringKey, String>(StringKey::class.java)
    overrides.configure()
    return Strings(overrides)
}

private val EN = Strings()

private val FR: Strings by lazy {
    stringsOf {
        put(StringKey.NavHome, "Accueil")
        put(StringKey.NavLive, "TV en direct")
        put(StringKey.NavMovies, "Films")
        put(StringKey.NavSeries, "Séries")
        put(StringKey.NavFavorites, "Favoris")
        put(StringKey.NavSearch, "Recherche")
        put(StringKey.NavCategories, "Catégories")
        put(StringKey.NavMultiview, "Multi-vue")
        put(StringKey.NavRecordings, "Enregistrements")
        put(StringKey.NavSettings, "Paramètres")
        put(StringKey.HomeWelcome, "Bienvenue dans Tviito TV")
        put(StringKey.HomeSubtitle, "Build native · prêt pour la télécommande")
        put(StringKey.HomeContinueWatching, "Continuer à regarder")
        put(StringKey.HomeRecentlyWatched, "Récemment regardé")
        put(StringKey.HomeFeaturedChannels, "Chaînes en vedette")
        put(StringKey.HomeFeaturedMovies, "Films")
        put(StringKey.HomeFeaturedSeries, "Séries")
        put(StringKey.OnboardingMacLabel, "MAC de l'appareil :")
        put(StringKey.OnboardingOpenSettings, "Ouvrir les paramètres")
        put(StringKey.OnboardingFirstTime, "Première configuration")
        put(StringKey.OnboardingTwoPaths, "Deux façons d'ajouter un fournisseur :")
        put(StringKey.OnboardingPathManual, "1. Ouvre Paramètres → +Xtream / +M3U / +M3U fichier / +Stalker et remplis le formulaire.")
        put(StringKey.OnboardingPathCloud, "2. Héberge le worker Cloudflare depuis cloudflare-config/, colle ce MAC dans son tableau de bord, puis Paramètres → Définir l'URL du worker → Sync depuis le cloud.")
        put(StringKey.ParentalPinEnabled, "PIN parental : activé (4 chiffres)")
        put(StringKey.ParentalPinNotSet, "PIN parental : non défini")
        put(StringKey.ParentalSetPin, "Définir le PIN")
        put(StringKey.ParentalChangePin, "Modifier le PIN")
        put(StringKey.ParentalClearPin, "Effacer")
        put(StringKey.ParentalManageLocked, "Gérer les chaînes verrouillées…")
        put(StringKey.ParentalSetTitle, "Définir le PIN parental")
        put(StringKey.ParentalPinHint, "PIN (4 chiffres)")
        put(StringKey.ParentalConfirmHint, "Confirmer le PIN")
        put(StringKey.ParentalLockedTitle, "🔒 Contenu verrouillé")
        put(StringKey.ParentalEnterPin, "Saisis ton PIN parental pour continuer.")
        put(StringKey.ParentalWrongPin, "PIN incorrect.")
        put(StringKey.ParentalUnlock, "Déverrouiller")
        put(StringKey.CategoriesManage, "Gérer les catégories")
        put(StringKey.CategoriesFilterHint, "Filtrer les noms de catégorie…")
        put(StringKey.CategoriesHideAll, "Masquer tout le filtre")
        put(StringKey.CategoriesShowAll, "Afficher tout le filtre")
        put(StringKey.CategoriesHideAdult, "🔞 Masquer adulte")
        put(StringKey.CategoriesResetAll, "Tout réinitialiser")
        put(StringKey.CategoriesEmpty, "Aucune catégorie — ajoute un fournisseur et re-sync.")
        put(StringKey.CategoriesShow, "Afficher")
        put(StringKey.CategoriesHide, "Masquer")
        put(StringKey.CategoriesCountTemplate, "%1\$d au total · %2\$d affichées · %3\$d masquées")
        put(StringKey.WizardWelcomeTitle, "👋 Bienvenue dans Tviito TV")
        put(StringKey.WizardAddProviderTitle, "📡 Ajouter un fournisseur")
        put(StringKey.WizardDoneTitle, "🎉 Tout est prêt")
        put(StringKey.WizardStepTemplate, "Étape %1\$d / %2\$d")
        put(StringKey.WizardIntro1, "Tviito TV est un client IPTV natif Android-TV. Il gère Xtream Codes, M3U / M3U8, fichiers M3U locaux et Stalker Portal.")
        put(StringKey.WizardIntro2, "Il utilise Compose-TV pour l'UI, Media3 / ExoPlayer pour la lecture et Room pour le catalogue. La navigation à la télécommande fonctionne nativement.")
        put(StringKey.WizardTwoPaths, "Deux options :")
        put(StringKey.WizardPathManual, "• Paramètres → +Xtream / +M3U URL / +M3U fichier / +Stalker. Remplis le formulaire.")
        put(StringKey.WizardPathCloud, "• Ou ouvre ton tableau de bord Cloudflare Worker, colle la MAC ci-dessous, ajoute tes fournisseurs, puis Paramètres → Sync depuis le cloud.")
        put(StringKey.WizardTipsHead, "Astuces à retrouver à tout moment :")
        put(StringKey.WizardTipDefault, "• ★ Le choix du fournisseur par défaut est dans Paramètres.")
        put(StringKey.WizardTipSleep, "• 💤 Veille programmée + 📊 Stats du flux dans l'overlay du lecteur.")
        put(StringKey.WizardTipLock, "• 🔒 Verrouille des chaînes via Paramètres → Parental.")
        put(StringKey.WizardTipBackup, "• 💾 Sauvegarde/restauration exporte fournisseurs + favoris + historique en JSON.")
        put(StringKey.WizardTipGuide, "• 🗓 Guide → Rafraîchir xmltv télécharge une grille EPG de 12 h.")
        put(StringKey.WizardBack, "Retour")
        put(StringKey.WizardNext, "Suivant")
        put(StringKey.WizardAddProviderCta, "Ajouter un fournisseur →")
        put(StringKey.WizardSkip, "Passer pour l'instant")
        put(StringKey.LockChannelsTitle, "Verrouiller des chaînes")
        put(StringKey.LockChannelsSubtitle, "Les chaînes verrouillées exigent le PIN parental. %1\$d au total · %2\$d verrouillées")
        put(StringKey.LockChannelsFilterHint, "Filtrer les chaînes…")
        put(StringKey.LockChannelsLock, "Verrouiller")
        put(StringKey.LockChannelsUnlock, "Déverrouiller")
        put(StringKey.GuideClickHint, "Clique une chaîne pour charger les prochains programmes.")
        put(StringKey.GuideLoadingEpg, "Chargement de l'EPG…")
        put(StringKey.GuideProgrammesTemplate, "%1\$d programmes chargés · %2\$d chaînes")
        put(StringKey.GuideLoading, "Chargement…")
        put(StringKey.GuideRefreshXmltv, "Rafraîchir xmltv")
        put(StringKey.GuideNoChannels, "Aucune chaîne — ajoute un fournisseur dans Paramètres.")
        put(StringKey.AddProviderAdd, "Ajouter")
        put(StringKey.AddProviderXtreamTitle, "Ajouter un fournisseur Xtream Codes")
        put(StringKey.AddProviderM3uTitle, "Ajouter une playlist M3U")
        put(StringKey.AddProviderStalkerTitle, "Ajouter un portail Stalker")
        put(StringKey.FieldNameOptional, "Nom (optionnel)")
        put(StringKey.FieldServerUrl, "URL du serveur (http://hôte:port)")
        put(StringKey.FieldUsername, "Identifiant")
        put(StringKey.FieldPassword, "Mot de passe")
        put(StringKey.FieldPlaylistUrl, "URL de la playlist")
        put(StringKey.FieldPortalUrl, "URL du portail")
        put(StringKey.FieldDeviceMac, "MAC de l'appareil (XX:XX:XX:XX:XX:XX)")
        put(StringKey.SettingsAutoImportTitle, "📡 Import auto via la MAC de l'appareil")
        put(StringKey.SettingsYourMac, "Ta MAC :")
        put(StringKey.SettingsMacHint, "Ouvre ton tableau de bord Worker, colle cette MAC, ajoute tes fournisseurs, puis appuie sur Sync.")
        put(StringKey.SettingsConfigPasswordLabel, "Mot de passe de la config : ")
        put(StringKey.SettingsConfigPasswordNone, "(aucun — n'importe qui avec la MAC peut lire)")
        put(StringKey.SettingsSet, "Définir")
        put(StringKey.SettingsSyncing, "En cours…")
        put(StringKey.SettingsSyncFromCloud, "Sync depuis le cloud")
        put(StringKey.SettingsAddProviderTitle, "➕ Ajouter un fournisseur")
        put(StringKey.SettingsAddProviderHint, "Appuie sur un bouton pour ouvrir le formulaire. Le clavier n'apparaît que dans la fenêtre — il ne te gênera pas en faisant défiler les paramètres.")
        put(StringKey.SettingsAddM3uUrl, "+ URL M3U")
        put(StringKey.SettingsAddM3uFile, "+ Fichier M3U…")
        put(StringKey.SettingsAddStalker, "+ Portail Stalker")
        put(StringKey.SettingsConfiguredHeader, "Fournisseurs configurés")
        put(StringKey.SettingsNoneYet, "(aucun pour l'instant)")
        put(StringKey.SettingsDefaultBadge, "★ Défaut")
        put(StringKey.SettingsSetDefault, "Définir par défaut")
        put(StringKey.SettingsBackupTitle, "💾 Sauvegarde et restauration")
        put(StringKey.SettingsBackupHint, "Exporte fournisseurs, favoris et historique dans un fichier JSON. Les catalogues (chaînes / films / séries) sont re-téléchargés à la sync, pas inclus.")
        put(StringKey.SettingsBackupExport, "Exporter la sauvegarde")
        put(StringKey.SettingsBackupImport, "Importer une sauvegarde…")
        put(StringKey.SettingsParentalHint, "Quand un PIN est défini, les catégories adultes (xxx / adult / 18+ / etc.) se verrouillent à chaque sync.")
        put(StringKey.SettingsConfigPwdDialogTitle, "Mot de passe de la config")
        put(StringKey.SettingsConfigPwdDialogHint, "Chaque config par MAC peut être protégée par un mot de passe. Saisis la même valeur que celle que l'admin a définie pour ta MAC. Laisse vide pour n'envoyer aucun mot de passe (n'a d'effet que pour les MAC non protégées).")
        put(StringKey.SettingsConfigPwdFieldLabel, "Mot de passe (visible — adapté à la télécommande)")
        put(StringKey.SettingsConfigPwdFieldPlaceholder, "Laisse vide pour effacer")
        put(StringKey.SettingsWorkerDialogTitle, "Définir l'URL du Worker Cloudflare")
        put(StringKey.SettingsWorkerDialogHint, "Chaque utilisateur déploie son propre Worker (cloudflare-config/) et colle son URL ici. Aucune URL par défaut n'est livrée dans l'app pour éviter de divulguer la tienne via le code source.")
        put(StringKey.SettingsWorkerFieldLabel, "URL de base du Worker (ex. https://ta-config.ton-compte.workers.dev)")
        put(StringKey.ToastBackupSaved, "Sauvegarde enregistrée")
        put(StringKey.ToastSaveFailed, "Échec de l'enregistrement : ")
        put(StringKey.ToastEmptyFile, "Fichier vide ou illisible")
        put(StringKey.ToastConfigPasswordSaved, "Mot de passe enregistré")
        put(StringKey.PrefSidebar, "Barre latérale")
        put(StringKey.PrefTopBar, "Barre du haut")
        put(StringKey.PrefThemeDark, "Sombre")
        put(StringKey.PrefThemeBlue, "Bleu")
        put(StringKey.PrefDefaultPlayer, "Lecteur par défaut")
        put(StringKey.PrefPlayerInternal, "Interne (Media3)")
        put(StringKey.PrefPlayerExternal, "Externe (VLC / MX)")
        put(StringKey.PrefAutoSyncHint, "Recharge les catalogues à chaque lancement de l'app.")
        put(StringKey.PrefShowChannelNumbers, "Afficher les numéros de chaîne")
        put(StringKey.PrefShowChannelNumbersHint, "Affiche le numéro de position à côté de chaque chaîne en Live TV.")
        put(StringKey.PrefHideAdult, "Masquer les catégories adultes")
        put(StringKey.PrefHideAdultHint, "Retire complètement les catégories adultes des listes (au-delà du verrou PIN).")
        put(StringKey.PrefResume, "Reprise de lecture")
        put(StringKey.PrefResumeHint, "Rouvre films/épisodes là où tu t'es arrêté.")
        put(StringKey.PrefAutoPlayNext, "Auto-lecture épisode suivant")
        put(StringKey.PrefAutoPlayNextHint, "Lance S0xE0y+1 automatiquement à la fin d'un épisode.")
        put(StringKey.PrefLaunchAtBoot, "Lancer au démarrage de la TV")
        put(StringKey.PrefLaunchAtBootHint, "Ouvre Tviito TV automatiquement quand la box démarre.")
        put(StringKey.PrefAutoPlayLast, "Reprendre le dernier au démarrage")
        put(StringKey.PrefAutoPlayLastHint, "Rejoue la dernière chaîne / film / épisode au lancement.")
        put(StringKey.PrefIntervalLaunch, "À chaque lancement")
        put(StringKey.PrefInterval6, "Toutes les 6 h")
        put(StringKey.PrefInterval12, "Toutes les 12 h")
        put(StringKey.PrefInterval24, "Toutes les 24 h")
        put(StringKey.FavoritesEmpty, "Aucun favori — ouvre un film ou une série et appuie sur ☆.")
        put(StringKey.FavoritesMoviesSection, "Films — %1\$d")
        put(StringKey.FavoritesSeriesSection, "Séries — %1\$d")
        put(StringKey.DetailLoading, "Chargement…")
        put(StringKey.SeriesNoEpisodes, "Aucun épisode disponible.")
        put(StringKey.MultiViewTitle, "Multi-vue")
        put(StringKey.MultiViewHint, "Sélectionne une case pour lui attribuer une chaîne. ENTRÉE passe à la suivante.")
        put(StringKey.MultiViewPickTemplate, "Choisis une chaîne pour la case %1\$d :")
        put(StringKey.RecordingsPlay, "Lire")
        put(StringKey.RecordingsOpenWith, "Ouvrir avec…")
        put(StringKey.PlayerOff, "Désactivé")
        put(StringKey.PlayerSubtitlesTemplate, "Sous-titres (%1\$d)")
        put(StringKey.LiveAllChannels, "Toutes les chaînes")
        put(StringKey.LiveChannelsCountTemplate, "%1\$d chaînes")
        put(StringKey.LiveNoChannelsInCategory, "Aucune chaîne dans cette catégorie.")
        put(StringKey.LiveOnAirPill, "EN COURS")
        put(StringKey.LiveThen, "puis")
        put(StringKey.LiveDayScheduleEyebrow, "PROGRAMME DE LA JOURNÉE")
        put(StringKey.LiveNoEpgForChannel, "Pas d'EPG disponible pour cette chaîne.")
        put(StringKey.UpdateAvailableEyebrow, "MISE À JOUR DISPONIBLE")
        put(StringKey.UpdateLater, "Plus tard")
        put(StringKey.UpdateInstall, "Mettre à jour")
        put(StringKey.UpdateDownloading, "Téléchargement…")
        put(StringKey.SettingsTelemetryTitle, "Diagnostics distants")
        put(StringKey.SettingsTelemetryHint, "Envoie crashes + events au worker pour debug. Désactive pour stopper toute télémétrie sortante.")
        put(StringKey.SettingsCheckForUpdates, "Vérifier les mises à jour")
        put(StringKey.SettingsCheckingForUpdates, "Vérification…")
        put(StringKey.SettingsUpToDateTemplate, "Vous êtes à jour (v%s)")
        put(StringKey.SettingsUpdateAvailableTemplate, "Mise à jour %s disponible")
        put(StringKey.BackupEncryptHint, "Le fichier exporté contient tes credentials Xtream/Stalker en clair. Saisis un mot de passe pour chiffrer le backup en AES-GCM (recommandé).")
        put(StringKey.BackupEncryptFieldLabel, "Mot de passe de chiffrement (optionnel)")
        put(StringKey.BackupEncryptFieldPlaceholder, "Laisser vide pour un export en clair")
        put(StringKey.RailOther, "Autre")
        put(StringKey.Open, "Ouvrir")
        put(StringKey.SleepLabel, "Veille")
        put(StringKey.Sleep1h, "1 heure")
        put(StringKey.Sleep2h, "2 heures")
        put(StringKey.SleepCancel, "Annuler le minuteur")
        put(StringKey.SleepReached, "Minuteur atteint — lecture en pause")
        put(StringKey.RecordingQueuedTemplate, "Enregistrement en file (max %1\$d min)")
        put(StringKey.StatResolution, "Résolution")
        put(StringKey.StatVideoCodec, "Codec vidéo")
        put(StringKey.StatFrameRate, "Images/s")
        put(StringKey.StatVideoBitrate, "Bitrate vidéo")
        put(StringKey.StatAudioCodec, "Codec audio")
        put(StringKey.StatAudioChannels, "Canaux audio")
        put(StringKey.StatBuffered, "Tampon")
        put(StringKey.StatDroppedFrames, "Images perdues")
        put(StringKey.ToastBackupReady, "Sauvegarde prête — choisis un fichier pour l'enregistrer.")
        put(StringKey.ToastRestoredTemplate, "Restauré : %1\$d fournisseur(s), %2\$d favoris, %3\$d entrées d'historique")
        put(StringKey.ToastRestoreFailed, "Échec de la restauration : ")
        put(StringKey.ToastRecordingQueued, "Enregistrement en file — voir l'écran Enregistrements")
        put(StringKey.SettingsTitle, "Paramètres")
        put(StringKey.SettingsDisplay, "Affichage et lecture")
        put(StringKey.SettingsParental, "Contrôle parental")
        put(StringKey.SettingsBackup, "Sauvegarde et restauration")
        put(StringKey.SettingsLanguage, "Langue")
        put(StringKey.SettingsTheme, "Thème")
        put(StringKey.SettingsMenuPosition, "Position du menu")
        put(StringKey.SettingsAutoSync, "Sync auto au lancement")
        put(StringKey.SettingsRefreshPlaylists, "Rafraîchir les playlists")
        put(StringKey.MovieDetailPlot, "Synopsis")
        put(StringKey.SeriesDetailEpisodes, "Épisodes")
        put(StringKey.MoviesTitle, "Films")
        put(StringKey.SeriesTitle, "Séries")
        put(StringKey.NoMovies, "Aucun film — ajoute un fournisseur dans les paramètres puis re-sync.")
        put(StringKey.NoSeries, "Aucune série — ajoute un fournisseur dans les paramètres puis re-sync.")
        put(StringKey.PlayerSleep, "Veille")
        put(StringKey.PlayerTracks, "Pistes")
        put(StringKey.PlayerDisplay, "Affichage")
        put(StringKey.PlayerExternal, "Lecteur externe")
        put(StringKey.PlayerRecord, "Enregistrer")
        put(StringKey.PlayerAspect, "Format")
        put(StringKey.PlayerSpeed, "Vitesse")
        put(StringKey.PlayerZapHint, "▲ ▼ pour zapper")
        put(StringKey.SearchPlaceholder, "Saisis pour chercher chaînes, films, séries…")
        put(StringKey.SearchRecent, "Récents :")
        put(StringKey.SearchClear, "Effacer")
        put(StringKey.SearchNoMatches, "Aucun résultat.")
        put(StringKey.RecordingsTitle, "Enregistrements")
        put(StringKey.RecordingsEmpty, "Aucun enregistrement pour l'instant. Ouvre un film ou un épisode et appuie sur ⏺ Enregistrer.")
        put(StringKey.RecordingStatusQueued, "En file")
        put(StringKey.RecordingStatusRunning, "Téléchargement…")
        put(StringKey.RecordingStatusDone, "Enregistré")
        put(StringKey.RecordingStatusFailed, "Échec")
        put(StringKey.RecordingStatusCancelled, "Annulé")
        put(StringKey.Live, "TV en direct")
        put(StringKey.Movies, "Films")
        put(StringKey.Series, "Séries")
        put(StringKey.Categories, "Catégories")
        put(StringKey.TvGuide, "Guide TV")
        put(StringKey.Favorites, "Favoris")
        put(StringKey.Play, "Lecture")
        put(StringKey.Resume, "Reprendre")
        put(StringKey.Cancel, "Annuler")
        put(StringKey.Close, "Fermer")
        put(StringKey.Save, "Enregistrer")
        put(StringKey.Delete, "Supprimer")
        put(StringKey.Confirm, "Confirmer")
        put(StringKey.Dismiss, "Retirer")
        put(StringKey.Change, "Modifier")
    }
}
private val ES: Strings by lazy {
    stringsOf {
        put(StringKey.NavHome, "Inicio")
        put(StringKey.NavLive, "TV en vivo")
        put(StringKey.NavGuide, "Guía")
        put(StringKey.NavMovies, "Películas")
        put(StringKey.NavFavorites, "Favoritos")
        put(StringKey.NavSearch, "Buscar")
        put(StringKey.NavCategories, "Categorías")
        put(StringKey.NavMultiview, "Multi-vista")
        put(StringKey.NavRecordings, "Grabaciones")
        put(StringKey.NavSettings, "Ajustes")
        put(StringKey.HomeWelcome, "Bienvenido a Tviito TV")
        put(StringKey.HomeSubtitle, "Build nativo · listo para mando a distancia")
        put(StringKey.HomeContinueWatching, "Continuar viendo")
        put(StringKey.HomeRecentlyWatched, "Vistos recientemente")
        put(StringKey.HomeFeaturedChannels, "Canales destacados")
        put(StringKey.HomeFeaturedMovies, "Películas")
        put(StringKey.OnboardingMacLabel, "MAC del dispositivo:")
        put(StringKey.OnboardingOpenSettings, "Abrir ajustes")
        put(StringKey.OnboardingFirstTime, "Configuración inicial")
        put(StringKey.OnboardingTwoPaths, "Dos formas de añadir un proveedor:")
        put(StringKey.OnboardingPathManual, "1. Abre Ajustes → +Xtream / +M3U / +M3U archivo / +Stalker y rellena el formulario.")
        put(StringKey.OnboardingPathCloud, "2. Aloja el worker de Cloudflare desde cloudflare-config/, pega esta MAC en su panel y luego Ajustes → Definir URL del worker → Sync desde la nube.")
        put(StringKey.ParentalPinEnabled, "PIN parental: activado (4 dígitos)")
        put(StringKey.ParentalPinNotSet, "PIN parental: no configurado")
        put(StringKey.ParentalSetPin, "Configurar PIN")
        put(StringKey.ParentalChangePin, "Cambiar PIN")
        put(StringKey.ParentalClearPin, "Borrar")
        put(StringKey.ParentalManageLocked, "Gestionar canales bloqueados…")
        put(StringKey.ParentalSetTitle, "Configurar PIN parental")
        put(StringKey.ParentalPinHint, "PIN (4 dígitos)")
        put(StringKey.ParentalConfirmHint, "Confirmar PIN")
        put(StringKey.ParentalLockedTitle, "🔒 Contenido bloqueado")
        put(StringKey.ParentalEnterPin, "Introduce tu PIN parental para continuar.")
        put(StringKey.ParentalWrongPin, "PIN incorrecto.")
        put(StringKey.ParentalUnlock, "Desbloquear")
        put(StringKey.CategoriesManage, "Gestionar categorías")
        put(StringKey.CategoriesFilterHint, "Filtrar nombres de categoría…")
        put(StringKey.CategoriesHideAll, "Ocultar todo el filtro")
        put(StringKey.CategoriesShowAll, "Mostrar todo el filtro")
        put(StringKey.CategoriesHideAdult, "🔞 Ocultar adulto")
        put(StringKey.CategoriesResetAll, "Restablecer todo")
        put(StringKey.CategoriesEmpty, "Sin categorías — añade un proveedor y vuelve a sincronizar.")
        put(StringKey.CategoriesShow, "Mostrar")
        put(StringKey.CategoriesHide, "Ocultar")
        put(StringKey.CategoriesCountTemplate, "%1\$d en total · %2\$d visibles · %3\$d ocultas")
        put(StringKey.WizardWelcomeTitle, "👋 Bienvenido a Tviito TV")
        put(StringKey.WizardAddProviderTitle, "📡 Añadir un proveedor")
        put(StringKey.WizardDoneTitle, "🎉 Todo listo")
        put(StringKey.WizardStepTemplate, "Paso %1\$d / %2\$d")
        put(StringKey.WizardIntro1, "Tviito TV es un cliente IPTV nativo para Android-TV. Soporta Xtream Codes, M3U / M3U8, archivos M3U locales y Stalker Portal.")
        put(StringKey.WizardIntro2, "Usa Compose-TV para la UI, Media3 / ExoPlayer para reproducción y Room para el catálogo. La navegación con mando funciona de fábrica.")
        put(StringKey.WizardTwoPaths, "Dos formas:")
        put(StringKey.WizardPathManual, "• Ajustes → +Xtream / +M3U URL / +M3U archivo / +Stalker. Rellena el formulario.")
        put(StringKey.WizardPathCloud, "• O abre tu panel de Cloudflare Worker, pega la MAC de abajo, añade los proveedores y luego Ajustes → Sync desde la nube.")
        put(StringKey.WizardTipsHead, "Consejos a los que puedes volver:")
        put(StringKey.WizardTipDefault, "• ★ El proveedor por defecto se cambia en Ajustes.")
        put(StringKey.WizardTipSleep, "• 💤 Temporizador + 📊 Stats del stream en el overlay del reproductor.")
        put(StringKey.WizardTipLock, "• 🔒 Bloquea canales en Ajustes → Parental.")
        put(StringKey.WizardTipBackup, "• 💾 Copia y restauración exporta proveedores + favoritos + historial como JSON.")
        put(StringKey.WizardTipGuide, "• 🗓 Guía → Refrescar xmltv descarga una rejilla EPG de 12 h.")
        put(StringKey.WizardBack, "Atrás")
        put(StringKey.WizardNext, "Siguiente")
        put(StringKey.WizardAddProviderCta, "Añadir un proveedor →")
        put(StringKey.WizardSkip, "Omitir por ahora")
        put(StringKey.LockChannelsTitle, "Bloquear canales individuales")
        put(StringKey.LockChannelsSubtitle, "Los canales bloqueados requieren el PIN parental. %1\$d en total · %2\$d bloqueados")
        put(StringKey.LockChannelsFilterHint, "Filtrar canales…")
        put(StringKey.LockChannelsLock, "Bloquear")
        put(StringKey.LockChannelsUnlock, "Desbloquear")
        put(StringKey.GuideClickHint, "Pulsa un canal para cargar los próximos programas.")
        put(StringKey.GuideLoadingEpg, "Cargando EPG…")
        put(StringKey.GuideProgrammesTemplate, "%1\$d programas cargados · %2\$d canales")
        put(StringKey.GuideLoading, "Cargando…")
        put(StringKey.GuideRefreshXmltv, "Refrescar xmltv")
        put(StringKey.GuideNoChannels, "Sin canales — añade un proveedor en Ajustes.")
        put(StringKey.AddProviderAdd, "Añadir")
        put(StringKey.AddProviderXtreamTitle, "Añadir proveedor Xtream Codes")
        put(StringKey.AddProviderM3uTitle, "Añadir playlist M3U")
        put(StringKey.AddProviderStalkerTitle, "Añadir portal Stalker")
        put(StringKey.FieldNameOptional, "Nombre (opcional)")
        put(StringKey.FieldServerUrl, "URL del servidor (http://host:puerto)")
        put(StringKey.FieldUsername, "Usuario")
        put(StringKey.FieldPassword, "Contraseña")
        put(StringKey.FieldPlaylistUrl, "URL de la playlist")
        put(StringKey.FieldPortalUrl, "URL del portal")
        put(StringKey.FieldDeviceMac, "MAC del dispositivo (XX:XX:XX:XX:XX:XX)")
        put(StringKey.SettingsAutoImportTitle, "📡 Importación automática mediante MAC")
        put(StringKey.SettingsYourMac, "Tu MAC:")
        put(StringKey.SettingsMacHint, "Abre el panel de tu worker, pega esta MAC, añade proveedores y pulsa Sync.")
        put(StringKey.SettingsConfigPasswordLabel, "Contraseña de la config: ")
        put(StringKey.SettingsConfigPasswordNone, "(ninguna — cualquiera con la MAC puede leerla)")
        put(StringKey.SettingsSet, "Definir")
        put(StringKey.SettingsSyncing, "Procesando…")
        put(StringKey.SettingsSyncFromCloud, "Sync desde la nube")
        put(StringKey.SettingsAddProviderTitle, "➕ Añadir un proveedor")
        put(StringKey.SettingsAddProviderHint, "Pulsa un botón para abrir el formulario. El teclado solo aparece dentro del diálogo — no molesta al desplazarte por Ajustes.")
        put(StringKey.SettingsAddM3uUrl, "+ URL M3U")
        put(StringKey.SettingsAddM3uFile, "+ Archivo M3U…")
        put(StringKey.SettingsAddStalker, "+ Portal Stalker")
        put(StringKey.SettingsConfiguredHeader, "Proveedores configurados")
        put(StringKey.SettingsNoneYet, "(ninguno aún)")
        put(StringKey.SettingsDefaultBadge, "★ Predeterminado")
        put(StringKey.SettingsSetDefault, "Hacer predeterminado")
        put(StringKey.SettingsBackupTitle, "💾 Copia y restauración")
        put(StringKey.SettingsBackupHint, "Exporta proveedores, favoritos e historial a un JSON que elijas. Los catálogos (canales / películas / series) se re-descargan en sync, no se incluyen.")
        put(StringKey.SettingsBackupExport, "Exportar copia")
        put(StringKey.SettingsBackupImport, "Importar copia…")
        put(StringKey.SettingsParentalHint, "Cuando hay un PIN, las categorías adultas (xxx / adult / 18+ / etc.) se bloquean en cada sync.")
        put(StringKey.SettingsConfigPwdDialogTitle, "Contraseña de la config")
        put(StringKey.SettingsConfigPwdDialogHint, "La config por MAC puede protegerse con contraseña. Usa la misma que el admin definió para tu MAC. Déjala vacía para no enviar contraseña (solo funciona con MAC sin proteger).")
        put(StringKey.SettingsConfigPwdFieldLabel, "Contraseña (visible — apta para mando)")
        put(StringKey.SettingsConfigPwdFieldPlaceholder, "Vacío para borrar")
        put(StringKey.SettingsWorkerDialogTitle, "Configurar URL del Worker Cloudflare")
        put(StringKey.SettingsWorkerDialogHint, "Cada usuario despliega su propio Worker (cloudflare-config/) y pega su URL aquí. No incluimos una por defecto para no filtrar la tuya en el código.")
        put(StringKey.SettingsWorkerFieldLabel, "URL base del Worker (p. ej. https://tu-config.tu-cuenta.workers.dev)")
        put(StringKey.ToastBackupSaved, "Copia guardada")
        put(StringKey.ToastSaveFailed, "Error al guardar: ")
        put(StringKey.ToastEmptyFile, "Archivo vacío o ilegible")
        put(StringKey.ToastConfigPasswordSaved, "Contraseña guardada")
        put(StringKey.PrefSidebar, "Barra lateral")
        put(StringKey.PrefTopBar, "Barra superior")
        put(StringKey.PrefThemeDark, "Oscuro")
        put(StringKey.PrefThemeBlue, "Azul")
        put(StringKey.PrefDefaultPlayer, "Reproductor por defecto")
        put(StringKey.PrefPlayerInternal, "Interno (Media3)")
        put(StringKey.PrefPlayerExternal, "Externo (VLC / MX)")
        put(StringKey.PrefAutoSyncHint, "Re-descarga los catálogos cada vez que arranca la app.")
        put(StringKey.PrefShowChannelNumbers, "Mostrar números de canal")
        put(StringKey.PrefShowChannelNumbersHint, "Muestra el número de posición junto a cada canal en TV en vivo.")
        put(StringKey.PrefHideAdult, "Ocultar categorías adultas")
        put(StringKey.PrefHideAdultHint, "Quita por completo las categorías adultas de las listas (más allá del bloqueo PIN).")
        put(StringKey.PrefResume, "Reanudar reproducción")
        put(StringKey.PrefResumeHint, "Reabre películas/episodios donde los dejaste.")
        put(StringKey.PrefAutoPlayNext, "Reproducir siguiente episodio")
        put(StringKey.PrefAutoPlayNextHint, "Reproduce S0xE0y+1 automáticamente al acabar un episodio.")
        put(StringKey.PrefLaunchAtBoot, "Iniciar al encender la TV")
        put(StringKey.PrefLaunchAtBootHint, "Abre Tviito TV automáticamente cuando la box arranca.")
        put(StringKey.PrefAutoPlayLast, "Reproducir último al inicio")
        put(StringKey.PrefAutoPlayLastHint, "Reanuda el último canal / película / episodio al iniciar.")
        put(StringKey.PrefIntervalLaunch, "Cada inicio")
        put(StringKey.PrefInterval6, "Cada 6 h")
        put(StringKey.PrefInterval12, "Cada 12 h")
        put(StringKey.PrefInterval24, "Cada 24 h")
        put(StringKey.FavoritesEmpty, "Sin favoritos — abre una película o serie y pulsa ☆.")
        put(StringKey.FavoritesMoviesSection, "Películas — %1\$d")
        put(StringKey.DetailLoading, "Cargando…")
        put(StringKey.SeriesNoEpisodes, "No hay episodios disponibles.")
        put(StringKey.MultiViewTitle, "Multi-vista")
        put(StringKey.MultiViewHint, "Pulsa una casilla para asignar un canal. ENTER pasa entre casillas.")
        put(StringKey.MultiViewPickTemplate, "Elige un canal para la casilla %1\$d:")
        put(StringKey.RecordingsPlay, "Reproducir")
        put(StringKey.RecordingsOpenWith, "Abrir con…")
        put(StringKey.PlayerOff, "Apagado")
        put(StringKey.PlayerSubtitlesTemplate, "Subtítulos (%1\$d)")
        put(StringKey.LiveAllChannels, "Todos los canales")
        put(StringKey.LiveChannelsCountTemplate, "%1\$d canales")
        put(StringKey.LiveNoChannelsInCategory, "Sin canales en esta categoría.")
        put(StringKey.LiveOnAirPill, "EN VIVO")
        put(StringKey.LiveThen, "después")
        put(StringKey.LiveDayScheduleEyebrow, "PROGRAMA DEL DÍA")
        put(StringKey.LiveNoEpgForChannel, "EPG no disponible para este canal.")
        put(StringKey.UpdateAvailableEyebrow, "ACTUALIZACIÓN DISPONIBLE")
        put(StringKey.UpdateLater, "Más tarde")
        put(StringKey.UpdateInstall, "Instalar")
        put(StringKey.UpdateDownloading, "Descargando…")
        put(StringKey.SettingsTelemetryTitle, "Diagnósticos remotos")
        put(StringKey.SettingsTelemetryHint, "Envía crashes y eventos al worker para depuración. Desactiva para detener toda telemetría.")
        put(StringKey.SettingsCheckForUpdates, "Buscar actualizaciones")
        put(StringKey.SettingsCheckingForUpdates, "Comprobando…")
        put(StringKey.SettingsUpToDateTemplate, "Estás al día (v%s)")
        put(StringKey.SettingsUpdateAvailableTemplate, "Actualización %s disponible")
        put(StringKey.BackupEncryptHint, "El archivo exportado contiene tus credenciales Xtream / Stalker en texto plano. Pon una contraseña para cifrar el backup con AES-GCM (recomendado).")
        put(StringKey.BackupEncryptFieldLabel, "Contraseña de cifrado (opcional)")
        put(StringKey.BackupEncryptFieldPlaceholder, "Vacío para una exportación en claro")
        put(StringKey.RailOther, "Otros")
        put(StringKey.Open, "Abrir")
        put(StringKey.SleepLabel, "Suspender")
        put(StringKey.Sleep1h, "1 hora")
        put(StringKey.Sleep2h, "2 horas")
        put(StringKey.SleepCancel, "Cancelar temporizador")
        put(StringKey.SleepReached, "Temporizador agotado — reproducción pausada")
        put(StringKey.RecordingQueuedTemplate, "Grabación en cola (máx %1\$d min)")
        put(StringKey.StatResolution, "Resolución")
        put(StringKey.StatVideoCodec, "Códec vídeo")
        put(StringKey.StatFrameRate, "Fotogramas/s")
        put(StringKey.StatVideoBitrate, "Bitrate vídeo")
        put(StringKey.StatAudioCodec, "Códec audio")
        put(StringKey.StatAudioChannels, "Canales audio")
        put(StringKey.StatBuffered, "Buffer")
        put(StringKey.StatDroppedFrames, "Fotogramas perdidos")
        put(StringKey.ToastBackupReady, "Copia lista — elige un archivo para guardarla.")
        put(StringKey.ToastRestoredTemplate, "Restaurado: %1\$d proveedor(es), %2\$d favoritos, %3\$d entradas de historial")
        put(StringKey.ToastRestoreFailed, "Error al restaurar: ")
        put(StringKey.ToastRecordingQueued, "Grabación en cola — ver pantalla Grabaciones")
        put(StringKey.SettingsTitle, "Ajustes")
        put(StringKey.SettingsDisplay, "Pantalla y reproducción")
        put(StringKey.SettingsParental, "Control parental")
        put(StringKey.SettingsBackup, "Copia y restauración")
        put(StringKey.SettingsLanguage, "Idioma")
        put(StringKey.SettingsTheme, "Tema")
        put(StringKey.SettingsMenuPosition, "Posición del menú")
        put(StringKey.SettingsAutoSync, "Sync auto al inicio")
        put(StringKey.SettingsRefreshPlaylists, "Actualizar listas")
        put(StringKey.MovieDetailPlot, "Sinopsis")
        put(StringKey.SeriesDetailEpisodes, "Episodios")
        put(StringKey.MoviesTitle, "Películas")
        put(StringKey.NoMovies, "Sin películas — añade un proveedor en ajustes y vuelve a sincronizar.")
        put(StringKey.NoSeries, "Sin series — añade un proveedor en ajustes y vuelve a sincronizar.")
        put(StringKey.PlayerSleep, "Suspender")
        put(StringKey.PlayerTracks, "Pistas")
        put(StringKey.PlayerDisplay, "Pantalla")
        put(StringKey.PlayerExternal, "Reproductor externo")
        put(StringKey.PlayerRecord, "Grabar")
        put(StringKey.PlayerAspect, "Aspecto")
        put(StringKey.PlayerSpeed, "Velocidad")
        put(StringKey.PlayerZapHint, "▲ ▼ para cambiar de canal")
        put(StringKey.SearchPlaceholder, "Escribe para buscar canales, películas, series…")
        put(StringKey.SearchRecent, "Recientes:")
        put(StringKey.SearchClear, "Limpiar")
        put(StringKey.SearchNoMatches, "Sin coincidencias.")
        put(StringKey.RecordingsTitle, "Grabaciones")
        put(StringKey.RecordingsEmpty, "Aún no hay grabaciones. Abre una película o episodio y pulsa ⏺ Grabar.")
        put(StringKey.RecordingStatusQueued, "En cola")
        put(StringKey.RecordingStatusRunning, "Descargando…")
        put(StringKey.RecordingStatusDone, "Guardado")
        put(StringKey.RecordingStatusFailed, "Falló")
        put(StringKey.RecordingStatusCancelled, "Cancelado")
        put(StringKey.Live, "TV en vivo")
        put(StringKey.Movies, "Películas")
        put(StringKey.Categories, "Categorías")
        put(StringKey.TvGuide, "Guía TV")
        put(StringKey.Favorites, "Favoritos")
        put(StringKey.Play, "Reproducir")
        put(StringKey.Resume, "Reanudar")
        put(StringKey.Cancel, "Cancelar")
        put(StringKey.Close, "Cerrar")
        put(StringKey.Save, "Guardar")
        put(StringKey.Delete, "Eliminar")
        put(StringKey.Confirm, "Confirmar")
        put(StringKey.Dismiss, "Descartar")
        put(StringKey.Change, "Cambiar")
    }
}
private val AR: Strings by lazy {
    stringsOf {
        put(StringKey.NavHome, "الرئيسية")
        put(StringKey.NavLive, "البث المباشر")
        put(StringKey.NavGuide, "الدليل")
        put(StringKey.NavMovies, "الأفلام")
        put(StringKey.NavSeries, "المسلسلات")
        put(StringKey.NavFavorites, "المفضلة")
        put(StringKey.NavSearch, "بحث")
        put(StringKey.NavCategories, "الفئات")
        put(StringKey.NavMultiview, "عرض متعدد")
        put(StringKey.NavRecordings, "التسجيلات")
        put(StringKey.NavSettings, "الإعدادات")
        put(StringKey.HomeWelcome, "مرحبًا بكم في Tviito TV")
        put(StringKey.HomeSubtitle, "نسخة أصلية · جاهزة لجهاز التحكم")
        put(StringKey.HomeContinueWatching, "متابعة المشاهدة")
        put(StringKey.HomeRecentlyWatched, "شوهد مؤخرًا")
        put(StringKey.HomeFeaturedChannels, "قنوات مميزة")
        put(StringKey.HomeFeaturedMovies, "الأفلام")
        put(StringKey.HomeFeaturedSeries, "المسلسلات")
        put(StringKey.OnboardingMacLabel, "عنوان MAC للجهاز:")
        put(StringKey.OnboardingOpenSettings, "فتح الإعدادات")
        put(StringKey.OnboardingFirstTime, "الإعداد الأول")
        put(StringKey.OnboardingTwoPaths, "طريقتان لإضافة موفّر:")
        put(StringKey.OnboardingPathManual, "1. افتح الإعدادات ← +Xtream / +M3U / +M3U ملف / +Stalker واملأ النموذج.")
        put(StringKey.OnboardingPathCloud, "2. استضف Cloudflare Worker من cloudflare-config/، ألصق عنوان MAC هذا في لوحة التحكم، ثم الإعدادات ← تعيين رابط الـ worker ← مزامنة من السحابة.")
        put(StringKey.ParentalPinEnabled, "PIN الرقابة الأبوية: مفعّل (٤ أرقام)")
        put(StringKey.ParentalPinNotSet, "PIN الرقابة الأبوية: غير محدد")
        put(StringKey.ParentalSetPin, "تعيين PIN")
        put(StringKey.ParentalChangePin, "تغيير PIN")
        put(StringKey.ParentalClearPin, "مسح")
        put(StringKey.ParentalManageLocked, "إدارة القنوات المقفلة…")
        put(StringKey.ParentalSetTitle, "تعيين PIN الرقابة الأبوية")
        put(StringKey.ParentalPinHint, "PIN (٤ أرقام)")
        put(StringKey.ParentalConfirmHint, "تأكيد PIN")
        put(StringKey.ParentalLockedTitle, "🔒 محتوى مقفل")
        put(StringKey.ParentalEnterPin, "أدخل PIN الرقابة الأبوية للمتابعة.")
        put(StringKey.ParentalWrongPin, "PIN غير صحيح.")
        put(StringKey.ParentalUnlock, "فتح")
        put(StringKey.CategoriesManage, "إدارة الفئات")
        put(StringKey.CategoriesFilterHint, "تصفية أسماء الفئات…")
        put(StringKey.CategoriesHideAll, "إخفاء كل المصفى")
        put(StringKey.CategoriesShowAll, "إظهار كل المصفى")
        put(StringKey.CategoriesHideAdult, "🔞 إخفاء الكبار")
        put(StringKey.CategoriesResetAll, "إعادة تعيين الكل")
        put(StringKey.CategoriesEmpty, "لا توجد فئات بعد — أضف موفّرًا وزامن.")
        put(StringKey.CategoriesShow, "إظهار")
        put(StringKey.CategoriesHide, "إخفاء")
        put(StringKey.CategoriesCountTemplate, "%1\$d إجمالًا · %2\$d ظاهرة · %3\$d مخفية")
        put(StringKey.WizardWelcomeTitle, "👋 مرحبًا بك في Tviito TV")
        put(StringKey.WizardAddProviderTitle, "📡 إضافة موفّر")
        put(StringKey.WizardDoneTitle, "🎉 كل شيء جاهز")
        put(StringKey.WizardStepTemplate, "الخطوة %1\$d / %2\$d")
        put(StringKey.WizardIntro1, "Tviito TV عميل IPTV أصلي لأندرويد TV. يدعم Xtream Codes وM3U / M3U8 وملفات M3U المحلية وStalker Portal.")
        put(StringKey.WizardIntro2, "يستخدم Compose-TV للواجهة وMedia3 / ExoPlayer للتشغيل وRoom للكتالوج. التنقل بالريموت يعمل تلقائيًا.")
        put(StringKey.WizardTwoPaths, "طريقتان:")
        put(StringKey.WizardPathManual, "• الإعدادات ← +Xtream / +M3U URL / +M3U ملف / +Stalker. املأ النموذج.")
        put(StringKey.WizardPathCloud, "• أو افتح لوحة Cloudflare Worker، ألصق MAC أدناه، أضف الموفّرين، ثم الإعدادات ← مزامنة من السحابة.")
        put(StringKey.WizardTipsHead, "نصائح يمكنك العودة إليها لاحقًا:")
        put(StringKey.WizardTipDefault, "• ★ الموفّر الافتراضي يُحدَّد من الإعدادات.")
        put(StringKey.WizardTipSleep, "• 💤 مؤقت النوم + 📊 إحصائيات البث ضمن واجهة المشغل.")
        put(StringKey.WizardTipLock, "• 🔒 اقفل قنوات من الإعدادات ← الرقابة الأبوية.")
        put(StringKey.WizardTipBackup, "• 💾 النسخ الاحتياطي يصدّر الموفّرين + المفضلة + السجل بصيغة JSON.")
        put(StringKey.WizardTipGuide, "• 🗓 الدليل ← تحديث xmltv يجلب شبكة EPG لمدة 12 ساعة.")
        put(StringKey.WizardBack, "رجوع")
        put(StringKey.WizardNext, "التالي")
        put(StringKey.WizardAddProviderCta, "إضافة موفّر ←")
        put(StringKey.WizardSkip, "تخطٍّ الآن")
        put(StringKey.LockChannelsTitle, "قفل القنوات الفردية")
        put(StringKey.LockChannelsSubtitle, "القنوات المقفلة تتطلب PIN الرقابة الأبوية. %1\$d إجمالًا · %2\$d مقفلة")
        put(StringKey.LockChannelsFilterHint, "تصفية القنوات…")
        put(StringKey.LockChannelsLock, "قفل")
        put(StringKey.LockChannelsUnlock, "فتح")
        put(StringKey.GuideClickHint, "اضغط قناة لتحميل برامجها التالية.")
        put(StringKey.GuideLoadingEpg, "جاري تحميل EPG…")
        put(StringKey.GuideProgrammesTemplate, "%1\$d برنامج محمَّل · %2\$d قناة")
        put(StringKey.GuideLoading, "جاري التحميل…")
        put(StringKey.GuideRefreshXmltv, "تحديث xmltv")
        put(StringKey.GuideNoChannels, "لا توجد قنوات — أضف موفّرًا من الإعدادات.")
        put(StringKey.AddProviderAdd, "إضافة")
        put(StringKey.AddProviderXtreamTitle, "إضافة موفّر Xtream Codes")
        put(StringKey.AddProviderM3uTitle, "إضافة قائمة M3U")
        put(StringKey.AddProviderStalkerTitle, "إضافة بوابة Stalker")
        put(StringKey.FieldNameOptional, "الاسم (اختياري)")
        put(StringKey.FieldServerUrl, "رابط الخادم (http://host:port)")
        put(StringKey.FieldUsername, "اسم المستخدم")
        put(StringKey.FieldPassword, "كلمة المرور")
        put(StringKey.FieldPlaylistUrl, "رابط القائمة")
        put(StringKey.FieldPortalUrl, "رابط البوابة")
        put(StringKey.FieldDeviceMac, "MAC الجهاز (XX:XX:XX:XX:XX:XX)")
        put(StringKey.SettingsAutoImportTitle, "📡 استيراد تلقائي بواسطة MAC")
        put(StringKey.SettingsYourMac, "MAC الخاص بك:")
        put(StringKey.SettingsMacHint, "افتح لوحة Worker، ألصق هذا MAC، أضف الموفّرين ثم اضغط مزامنة.")
        put(StringKey.SettingsConfigPasswordLabel, "كلمة مرور الإعداد: ")
        put(StringKey.SettingsConfigPasswordNone, "(لا توجد — أي شخص يملك MAC يستطيع قراءتها)")
        put(StringKey.SettingsSet, "تعيين")
        put(StringKey.SettingsSyncing, "جارٍ العمل…")
        put(StringKey.SettingsSyncFromCloud, "مزامنة من السحابة")
        put(StringKey.SettingsAddProviderTitle, "➕ إضافة موفّر")
        put(StringKey.SettingsAddProviderHint, "اضغط زرًا لفتح النموذج. لوحة المفاتيح تظهر فقط داخل النافذة — لن تعترضك أثناء التمرير.")
        put(StringKey.SettingsAddM3uUrl, "+ رابط M3U")
        put(StringKey.SettingsAddM3uFile, "+ ملف M3U…")
        put(StringKey.SettingsAddStalker, "+ بوابة Stalker")
        put(StringKey.SettingsConfiguredHeader, "الموفّرون المهيّأون")
        put(StringKey.SettingsNoneYet, "(لا يوجد بعد)")
        put(StringKey.SettingsDefaultBadge, "★ افتراضي")
        put(StringKey.SettingsSetDefault, "تعيين كافتراضي")
        put(StringKey.SettingsResync, "إعادة مزامنة")
        put(StringKey.SettingsBackupTitle, "💾 النسخ الاحتياطي والاستعادة")
        put(StringKey.SettingsBackupHint, "يصدّر الموفّرين والمفضلة والسجل إلى ملف JSON تختاره. الكتالوجات (قنوات/أفلام/مسلسلات) تُحمَّل عبر المزامنة، وليست مضمّنة.")
        put(StringKey.SettingsBackupExport, "تصدير النسخة")
        put(StringKey.SettingsBackupImport, "استيراد نسخة…")
        put(StringKey.SettingsParentalHint, "عند تعيين PIN تُقفل الفئات الكبيرة (xxx / adult / 18+ / إلخ) تلقائيًا عند كل مزامنة.")
        put(StringKey.SettingsConfigPwdDialogTitle, "كلمة مرور الإعداد")
        put(StringKey.SettingsConfigPwdDialogHint, "يمكن حماية إعداد كل MAC بكلمة مرور. أدخل القيمة التي حدّدها المسؤول. اتركها فارغة لعدم إرسال كلمة مرور (تعمل فقط مع MAC غير محمي).")
        put(StringKey.SettingsConfigPwdFieldLabel, "كلمة المرور (مرئية — مناسبة للريموت)")
        put(StringKey.SettingsConfigPwdFieldPlaceholder, "اتركها فارغة لمسحها")
        put(StringKey.SettingsWorkerDialogTitle, "ضبط رابط Cloudflare Worker")
        put(StringKey.SettingsWorkerDialogHint, "كل مستخدم ينشر Worker الخاص به (cloudflare-config/) ويلصق رابطه هنا. لا نضمّن أي رابط افتراضي تجنبًا لتسريب رابطك عبر الشيفرة.")
        put(StringKey.SettingsWorkerFieldLabel, "رابط Worker الأساسي (مثال https://your-config.your-acct.workers.dev)")
        put(StringKey.ToastBackupSaved, "تم حفظ النسخة")
        put(StringKey.ToastSaveFailed, "فشل الحفظ: ")
        put(StringKey.ToastEmptyFile, "ملف فارغ أو غير مقروء")
        put(StringKey.ToastConfigPasswordSaved, "تم حفظ كلمة المرور")
        put(StringKey.PrefSidebar, "الشريط الجانبي")
        put(StringKey.PrefTopBar, "الشريط العلوي")
        put(StringKey.PrefThemeDark, "داكن")
        put(StringKey.PrefThemeBlue, "أزرق")
        put(StringKey.PrefDefaultPlayer, "المشغّل الافتراضي")
        put(StringKey.PrefPlayerInternal, "داخلي (Media3)")
        put(StringKey.PrefPlayerExternal, "خارجي (VLC / MX)")
        put(StringKey.PrefAutoSyncHint, "إعادة تحميل كتالوجات الموفّر مع كل تشغيل.")
        put(StringKey.PrefShowChannelNumbers, "إظهار أرقام القنوات")
        put(StringKey.PrefShowChannelNumbersHint, "إظهار رقم القناة بجانب كل قناة في البث المباشر.")
        put(StringKey.PrefHideAdult, "إخفاء فئات الكبار")
        put(StringKey.PrefHideAdultHint, "إزالة فئات الكبار من القوائم تمامًا (أوسع من قفل PIN).")
        put(StringKey.PrefResume, "متابعة التشغيل")
        put(StringKey.PrefResumeHint, "إعادة فتح الأفلام/الحلقات من حيث توقفت.")
        put(StringKey.PrefAutoPlayNext, "تشغيل تلقائي للحلقة التالية")
        put(StringKey.PrefAutoPlayNextHint, "تشغيل S0xE0y+1 تلقائيًا عند انتهاء الحلقة.")
        put(StringKey.PrefLaunchAtBoot, "التشغيل عند إقلاع التلفاز")
        put(StringKey.PrefLaunchAtBootHint, "فتح Tviito TV تلقائيًا عند انتهاء إقلاع الجهاز.")
        put(StringKey.PrefAutoPlayLast, "تشغيل آخر مشاهدة عند البدء")
        put(StringKey.PrefAutoPlayLastHint, "استئناف آخر قناة / فيلم / حلقة عند تشغيل التطبيق.")
        put(StringKey.PrefIntervalLaunch, "كل تشغيل")
        put(StringKey.PrefInterval6, "كل 6 ساعات")
        put(StringKey.PrefInterval12, "كل 12 ساعة")
        put(StringKey.PrefInterval24, "كل 24 ساعة")
        put(StringKey.FavoritesEmpty, "لا توجد مفضلة بعد — افتح فيلمًا أو مسلسلًا واضغط ☆.")
        put(StringKey.FavoritesMoviesSection, "الأفلام — %1\$d")
        put(StringKey.FavoritesSeriesSection, "المسلسلات — %1\$d")
        put(StringKey.DetailLoading, "جاري التحميل…")
        put(StringKey.SeriesNoEpisodes, "لا توجد حلقات متاحة.")
        put(StringKey.MultiViewTitle, "عرض متعدد")
        put(StringKey.MultiViewHint, "اضغط مربعًا لإسناد قناة. ENTER يتنقّل بين المربعات.")
        put(StringKey.MultiViewPickTemplate, "اختر قناة للمربع %1\$d:")
        put(StringKey.RecordingsPlay, "تشغيل")
        put(StringKey.RecordingsOpenWith, "فتح باستخدام…")
        put(StringKey.PlayerOff, "إيقاف")
        put(StringKey.PlayerAudioTemplate, "صوت (%1\$d)")
        put(StringKey.PlayerSubtitlesTemplate, "ترجمات (%1\$d)")
        put(StringKey.LiveAllChannels, "كل القنوات")
        put(StringKey.LiveChannelsCountTemplate, "%1\$d قناة")
        put(StringKey.LiveNoChannelsInCategory, "لا توجد قنوات في هذه الفئة.")
        put(StringKey.LiveZappingEyebrow, "تنقّل")
        put(StringKey.LiveOnAirPill, "مباشر")
        put(StringKey.LiveThen, "ثم")
        put(StringKey.LiveDayScheduleEyebrow, "برنامج اليوم")
        put(StringKey.LiveNoEpgForChannel, "لا يوجد دليل برامج لهذه القناة.")
        put(StringKey.UpdateAvailableEyebrow, "تحديث متاح")
        put(StringKey.UpdateLater, "لاحقا")
        put(StringKey.UpdateInstall, "تثبيت")
        put(StringKey.UpdateDownloading, "جار التنزيل…")
        put(StringKey.SettingsTelemetryTitle, "التشخيص عن بعد")
        put(StringKey.SettingsTelemetryHint, "إرسال سجلات الأعطال والأحداث إلى الخادم للتشخيص. إيقاف = لا قياس عن بعد.")
        put(StringKey.SettingsCheckForUpdates, "البحث عن تحديثات")
        put(StringKey.SettingsCheckingForUpdates, "جار الفحص…")
        put(StringKey.SettingsUpToDateTemplate, "أنت على أحدث إصدار (v%s)")
        put(StringKey.SettingsUpdateAvailableTemplate, "التحديث %s متاح")
        put(StringKey.BackupEncryptHint, "الملف المُصدَّر يحتوي على بيانات اعتماد Xtream / Stalker بدون تشفير. أدخل كلمة مرور لتشفير النسخة الاحتياطية بـ AES-GCM (موصى به).")
        put(StringKey.BackupEncryptFieldLabel, "كلمة مرور التشفير (اختياري)")
        put(StringKey.BackupEncryptFieldPlaceholder, "اتركها فارغة لتصدير بدون تشفير")
        put(StringKey.RailOther, "أخرى")
        put(StringKey.Open, "فتح")
        put(StringKey.SleepLabel, "النوم")
        put(StringKey.SleepMin15, "15 دقيقة")
        put(StringKey.SleepMin30, "30 دقيقة")
        put(StringKey.Sleep1h, "ساعة")
        put(StringKey.Sleep2h, "ساعتان")
        put(StringKey.SleepCancel, "إلغاء المؤقت")
        put(StringKey.SleepReached, "انتهى مؤقت النوم — أُوقف التشغيل")
        put(StringKey.RecordingQueuedTemplate, "التسجيل في الانتظار (بحد أقصى %1\$d د)")
        put(StringKey.StatResolution, "الدقة")
        put(StringKey.StatVideoCodec, "كودك الفيديو")
        put(StringKey.StatFrameRate, "معدل الإطارات")
        put(StringKey.StatVideoBitrate, "بِت فيديو")
        put(StringKey.StatAudioCodec, "كودك الصوت")
        put(StringKey.StatAudioChannels, "قنوات الصوت")
        put(StringKey.StatBuffered, "المخزن المؤقت")
        put(StringKey.StatDroppedFrames, "الإطارات المفقودة")
        put(StringKey.ToastBackupReady, "النسخة جاهزة — اختر ملفًا لحفظها.")
        put(StringKey.ToastRestoredTemplate, "تمت الاستعادة: %1\$d موفّر، %2\$d مفضّل، %3\$d إدخالًا في السجل")
        put(StringKey.ToastRestoreFailed, "فشلت الاستعادة: ")
        put(StringKey.ToastRecordingQueued, "التسجيل في الانتظار — راجع شاشة التسجيلات")
        put(StringKey.SettingsTitle, "الإعدادات")
        put(StringKey.SettingsDisplay, "العرض والتشغيل")
        put(StringKey.SettingsParental, "الرقابة الأبوية")
        put(StringKey.SettingsBackup, "النسخ الاحتياطي والاستعادة")
        put(StringKey.SettingsLanguage, "اللغة")
        put(StringKey.SettingsTheme, "السمة")
        put(StringKey.SettingsMenuPosition, "موضع القائمة")
        put(StringKey.SettingsAutoSync, "المزامنة التلقائية عند البدء")
        put(StringKey.SettingsRefreshPlaylists, "تحديث القوائم")
        put(StringKey.MovieDetailPlot, "القصة")
        put(StringKey.SeriesDetailEpisodes, "الحلقات")
        put(StringKey.MoviesTitle, "الأفلام")
        put(StringKey.SeriesTitle, "المسلسلات")
        put(StringKey.NoMovies, "لا توجد أفلام — أضف موفّرًا من الإعدادات ثم زامن.")
        put(StringKey.NoSeries, "لا توجد مسلسلات — أضف موفّرًا من الإعدادات ثم زامن.")
        put(StringKey.PlayerSleep, "مؤقت النوم")
        put(StringKey.PlayerStats, "إحصائيات")
        put(StringKey.PlayerTracks, "المسارات")
        put(StringKey.PlayerDisplay, "العرض")
        put(StringKey.PlayerExternal, "مشغّل خارجي")
        put(StringKey.PlayerCast, "بث")
        put(StringKey.PlayerRecord, "تسجيل")
        put(StringKey.PlayerAspect, "نسبة العرض")
        put(StringKey.PlayerSpeed, "السرعة")
        put(StringKey.PlayerZapHint, "▲ ▼ لتغيير القناة")
        put(StringKey.SearchPlaceholder, "اكتب للبحث عن قنوات، أفلام، مسلسلات…")
        put(StringKey.SearchRecent, "الأخيرة:")
        put(StringKey.SearchClear, "مسح")
        put(StringKey.SearchNoMatches, "لا توجد نتائج.")
        put(StringKey.RecordingsTitle, "التسجيلات")
        put(StringKey.RecordingsEmpty, "لا توجد تسجيلات بعد. افتح فيلمًا أو حلقة واضغط ⏺ تسجيل.")
        put(StringKey.RecordingStatusQueued, "في الانتظار")
        put(StringKey.RecordingStatusRunning, "جاري التحميل…")
        put(StringKey.RecordingStatusDone, "محفوظ")
        put(StringKey.RecordingStatusFailed, "فشل")
        put(StringKey.RecordingStatusCancelled, "أُلغي")
        put(StringKey.Live, "البث المباشر")
        put(StringKey.Movies, "الأفلام")
        put(StringKey.Series, "المسلسلات")
        put(StringKey.Categories, "الفئات")
        put(StringKey.TvGuide, "دليل التلفاز")
        put(StringKey.Favorites, "المفضلة")
        put(StringKey.Play, "تشغيل")
        put(StringKey.Resume, "استئناف")
        put(StringKey.Cancel, "إلغاء")
        put(StringKey.Close, "إغلاق")
        put(StringKey.Save, "حفظ")
        put(StringKey.Delete, "حذف")
        put(StringKey.Confirm, "تأكيد")
        put(StringKey.Dismiss, "إهمال")
        put(StringKey.Change, "تغيير")
    }
}
@Composable
fun stringsFor(lang: AppLang): Strings {
    val resolved = if (lang == AppLang.System) {
        val sys = LocalConfiguration.current.locales.get(0)?.language ?: "en"
        AppLang.entries.firstOrNull { it.code == sys } ?: AppLang.English
    } else lang
    return when (resolved) {
        AppLang.French -> FR
        AppLang.Spanish -> ES
        AppLang.Arabic -> AR
        else -> EN
    }
}

val LocalStrings = compositionLocalOf<Strings> { EN }
