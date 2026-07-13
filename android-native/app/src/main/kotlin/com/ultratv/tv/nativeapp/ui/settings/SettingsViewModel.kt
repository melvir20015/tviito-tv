package com.ultratv.tv.nativeapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultratv.tv.nativeapp.data.db.ProviderEntity
import com.ultratv.tv.nativeapp.data.repo.ProviderRepository
import com.ultratv.tv.nativeapp.data.xtream.XtreamClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val providers: List<ProviderEntity> = emptyList(),
    val syncing: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: ProviderRepository,
    private val remoteConfig: com.ultratv.tv.nativeapp.data.config.RemoteConfigImporter,
    private val deviceMac: com.ultratv.tv.nativeapp.data.config.DeviceMac,
    private val prefs: com.ultratv.tv.nativeapp.data.prefs.UserPreferencesStore,
    private val backupRepo: com.ultratv.tv.nativeapp.data.repo.BackupRepository,
) : ViewModel() {

    /** Mirrors UserPrefs.localLogosFolderUri for the Settings UI to display. */
    val localLogosFolderUri: kotlinx.coroutines.flow.StateFlow<String> = prefs.flow
        .map { it.localLogosFolderUri }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), "")

    fun setLocalLogosFolderUri(uri: String) = viewModelScope.launch { prefs.setLocalLogosFolderUri(uri) }

    private val _backupText = MutableStateFlow<String?>(null)
    val backupText: StateFlow<String?> = _backupText.asStateFlow()

    fun prepareBackup(
        readyMsg: String = "Copia de seguridad lista — elige un archivo para guardarla.",
        password: String? = null,
    ) {
        viewModelScope.launch {
            _backupText.value = backupRepo.export(password)
            com.ultratv.tv.nativeapp.ui.common.Toaster.ok(readyMsg)
        }
    }

    fun consumeBackup(): String? {
        val t = _backupText.value
        _backupText.value = null
        return t
    }

    fun restoreBackup(
        text: String,
        restoredTemplate: String = "Restaurados %1\$d proveedor(es), %2\$d favorito(s), %3\$d entradas de historial",
        failedPrefix: String = "Error al restaurar: ",
        password: String? = null,
    ) {
        viewModelScope.launch {
            try {
                val r = backupRepo.import(text, password)
                com.ultratv.tv.nativeapp.ui.common.Toaster.ok(
                    restoredTemplate.format(r.providers, r.favorites, r.historyEntries)
                )
            } catch (t: Throwable) {
                com.ultratv.tv.nativeapp.ui.common.Toaster.err(failedPrefix + (t.message ?: ""))
            }
        }
    }

    val deviceMacAddress: String = deviceMac.mac

    /**
     * Effective Worker URL entered by the user. Empty by default so fork
     * builds never contact a project-hosted Cloudflare Worker automatically.
     */
    val workerBaseUrl: StateFlow<String> = prefs.flow
        .map { it.workerBaseUrl }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun saveWorkerBase(url: String) {
        viewModelScope.launch { prefs.setWorkerBase(url) }
    }

    val configPassword: StateFlow<String> = prefs.flow
        .map { it.configPassword }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun saveConfigPassword(pwd: String) {
        viewModelScope.launch { prefs.setConfigPassword(pwd) }
    }

    fun importByMac(workerBase: String) {
        viewModelScope.launch {
            _syncing.value = true
            _message.value = "Consultando configuración para la MAC ${deviceMac.mac}…"
            try {
                val res = remoteConfig.importByMac(
                    workerBase, deviceMac.mac, configPassword.value,
                ) { _message.value = it }
                // Ensure something becomes default so the UI has a provider to use.
                if (res.imported > 0 && repo.firstActive() == null) {
                    repo.observeProviders().first().firstOrNull()?.id?.let { repo.setDefault(it) }
                }
                _message.value = when {
                    res.imported == 0 && res.errors.isEmpty() ->
                        "No existe configuración para esta MAC. Ve a ${workerBase.trimEnd('/')} y configura ${deviceMac.mac}."
                    res.errors.isEmpty() -> "Importados ${res.imported} proveedor(es) ✓"
                    else -> "Importados ${res.imported} proveedor(es) · ${res.errors.size} error(es): ${res.errors.first()}"
                }
            } catch (e: com.ultratv.tv.nativeapp.data.config.RemoteConfigImporter.WrongPasswordException) {
                _message.value = "⚠ Contraseña de configuración incorrecta o ausente — configúrala en Ajustes."
            } catch (t: Throwable) {
                _message.value = syncErrorMessage(t)
            } finally {
                _syncing.value = false
            }
        }
    }

    fun importFromRemoteConfig(url: String) {
        viewModelScope.launch {
            _syncing.value = true
            _message.value = "Descargando configuración desde $url…"
            try {
                val res = remoteConfig.importFromUrl(url) { _message.value = it }
                if (res.imported > 0 && repo.firstActive() == null) {
                    repo.observeProviders().first().firstOrNull()?.id?.let { repo.setDefault(it) }
                }
                val errs = if (res.errors.isEmpty()) "" else "  ·  ${res.errors.size} error(es): ${res.errors.first()}"
                _message.value = "Importados ${res.imported} proveedor(es)$errs"
            } catch (t: Throwable) {
                _message.value = syncErrorMessage(t)
            } finally {
                _syncing.value = false
            }
        }
    }

    private val _message = MutableStateFlow<String?>(null)
    private val _syncing = MutableStateFlow(false)

    val providers: StateFlow<List<ProviderEntity>> =
        repo.observeProviders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val message: StateFlow<String?> = _message.asStateFlow()
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()


    private fun syncSuccessMessage(total: Int): String =
        if (total > 0) "Sincronización completa — $total elementos importados"
        else "Sincronización finalizada sin elementos importados."

    private fun syncErrorMessage(t: Throwable): String = when (t) {
        is XtreamClient.XtreamException.InvalidCredentials ->
            "Credenciales Xtream inválidas. Revisa usuario, contraseña y URL del servidor."
        is XtreamClient.XtreamException.ExpiredAccount ->
            "La cuenta Xtream está expirada. Contacta con tu proveedor."
        is XtreamClient.XtreamException.UnsupportedResponse ->
            "Servidor Xtream no compatible: ${t.message}"
        is XtreamClient.XtreamException.EmptyResponse ->
            "El servidor Xtream devolvió una respuesta vacía. Revisa la URL base y la compatibilidad con player_api.php."
        is XtreamClient.XtreamException.HtmlResponse ->
            "El servidor devolvió HTML en lugar de JSON; revisa la URL base o si el proveedor bloquea este cliente."
        is XtreamClient.XtreamException.Http ->
            "Error de red/HTTP: ${t.message}"
        is XtreamClient.XtreamException.Network ->
            "Error de red/HTTP: ${t.message}"
        is XtreamClient.XtreamException.InvalidJson ->
            "Servidor Xtream no compatible: ${t.message}"
        is XtreamClient.XtreamException.EmptyCatalog ->
            "Sincronización con cero elementos: ${t.message}"
        else -> "Error: ${t.message ?: "No se pudo completar la sincronización."}"
    }

    /** Promotes the new provider to default iff nothing else is active yet. */
    private suspend fun makeDefaultIfNone(newId: Long) {
        if (repo.firstActive() == null) repo.setDefault(newId)
    }

    fun setDefault(id: Long) {
        viewModelScope.launch {
            repo.setDefault(id)
            _message.value = "Proveedor predeterminado cambiado."
        }
    }

    fun addAndSync(name: String, baseUrl: String, username: String, password: String) {
        viewModelScope.launch {
            _syncing.value = true
            _message.value = "Agregando proveedor…"
            try {
                val id = repo.addXtream(name, baseUrl, username, password)
                makeDefaultIfNone(id)
                _message.value = "Sincronizando catálogo Xtream…"
                val n = repo.syncAll(id) { _message.value = it }
                _message.value = syncSuccessMessage(n)
            } catch (t: Throwable) {
                _message.value = syncErrorMessage(t)
            } finally {
                _syncing.value = false
            }
        }
    }

    fun addM3uLocal(name: String, label: String, text: String) {
        viewModelScope.launch {
            _syncing.value = true
            _message.value = "Importando M3U local…"
            try {
                val id = repo.addM3uFromText(name, label, text)
                makeDefaultIfNone(id)
                _message.value = "Importado — reinicia la pestaña TV en vivo para ver los canales."
            } catch (t: Throwable) {
                _message.value = syncErrorMessage(t)
            } finally {
                _syncing.value = false
            }
        }
    }

    fun addStalkerAndSync(name: String, portalUrl: String, mac: String) {
        viewModelScope.launch {
            _syncing.value = true
            _message.value = "Agregando portal Stalker…"
            try {
                val id = repo.addStalker(name, portalUrl, mac)
                makeDefaultIfNone(id)
                val n = repo.syncAll(id) { _message.value = it }
                _message.value = syncSuccessMessage(n)
            } catch (t: Throwable) {
                _message.value = syncErrorMessage(t)
            } finally {
                _syncing.value = false
            }
        }
    }

    fun addM3uAndSync(name: String, url: String) {
        viewModelScope.launch {
            _syncing.value = true
            _message.value = "Agregando proveedor M3U…"
            try {
                val id = repo.addM3u(name, url)
                makeDefaultIfNone(id)
                val n = repo.syncAll(id) { _message.value = it }
                _message.value = syncSuccessMessage(n)
            } catch (t: Throwable) {
                _message.value = syncErrorMessage(t)
            } finally {
                _syncing.value = false
            }
        }
    }

    fun resync(providerId: Long) {
        viewModelScope.launch {
            _syncing.value = true
            try {
                val n = repo.syncAll(providerId) { _message.value = it }
                _message.value = syncSuccessMessage(n)
            } catch (t: Throwable) {
                _message.value = syncErrorMessage(t)
            } finally {
                _syncing.value = false
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repo.delete(id)
            _message.value = "Proveedor eliminado"
        }
    }
}
