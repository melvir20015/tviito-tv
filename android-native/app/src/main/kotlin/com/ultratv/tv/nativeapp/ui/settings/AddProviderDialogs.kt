package com.ultratv.tv.nativeapp.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusGroup
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/**
 * Full-screen modal scrim hosting a form. Lets us keep the Settings list above
 * 100% text/buttons/switches so D-pad scroll never lands on a TextField that
 * would summon the IME mid-scroll. Inputs only ever appear when the user has
 * explicitly opened one of these dialogs.
 */
@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
fun AddProviderDialog(
    title: String,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    canSubmit: Boolean,
    initialFocusRequester: FocusRequester? = null,
    content: @Composable () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    val dialogFocusRequester = remember { FocusRequester() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        LaunchedEffect(initialFocusRequester, dialogFocusRequester) {
            // Wait until the dialog content has been placed in the modal
            // window before moving focus away from SettingsScreen. The
            // focusable group fallback keeps D-pad events in the modal even if
            // a specific TextField is not ready on the first frame.
            androidx.compose.runtime.withFrameNanos { }
            runCatching { dialogFocusRequester.requestFocus() }
            initialFocusRequester?.let { requester ->
                androidx.compose.runtime.withFrameNanos { }
                runCatching { requester.requestFocus() }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 480.dp, max = 720.dp)
                    .focusRequester(dialogFocusRequester)
                    .focusGroup()
                    .focusable()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val S = com.ultratv.tv.nativeapp.i18n.LocalStrings.current
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                content()
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button(
                        onClick = onSubmit,
                        enabled = canSubmit,
                    ) { Text(S.addProviderAdd, fontSize = 15.sp) }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                    ) { Text(S.cancel, fontSize = 15.sp) }
                }
            }
        }
    }
}

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String? = null,
    autoFocus: Boolean = false,
    focusRequester: FocusRequester? = null,
) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val internalFocusRequester = remember { FocusRequester() }
    val effectiveFocusRequester = focusRequester ?: internalFocusRequester
    // Grab focus the first time the field is shown when caller marks it as the
    // dialog's primary input. D-pad would otherwise stay on whatever was
    // focused behind the dialog, leaving the user unable to type.
    LaunchedEffect(autoFocus, effectiveFocusRequester) {
        if (autoFocus) {
            androidx.compose.runtime.withFrameNanos { }
            runCatching { effectiveFocusRequester.requestFocus() }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.background)
                .androidx_border(focused)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (password) KeyboardType.Password else keyboardType,
                ),
                interactionSource = interaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(effectiveFocusRequester),
                decorationBox = { inner ->
                    if (value.isEmpty() && placeholder != null) {
                        Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                    }
                    inner()
                },
            )
        }
    }
}

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
fun XtreamDialog(onDismiss: () -> Unit, onSubmit: (name: String, url: String, user: String, pass: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val canSubmit = url.isNotBlank() && user.isNotBlank() && pass.isNotBlank()
    val serverUrlFocusRequester = remember { FocusRequester() }
    val S = com.ultratv.tv.nativeapp.i18n.LocalStrings.current
    AddProviderDialog(
        title = S.addProviderXtreamTitle,
        onDismiss = onDismiss,
        onSubmit = { onSubmit(name, url, user, pass) },
        canSubmit = canSubmit,
        initialFocusRequester = serverUrlFocusRequester,
    ) {
        FormField(S.fieldNameOptional, name, { name = it })
        // The server URL is the first required field and the intended initial
        // focus target; the optional display name remains reachable with D-pad Up.
        FormField(S.fieldServerUrl, url, { url = it }, keyboardType = KeyboardType.Uri,
            placeholder = "http://provider.com:8080", autoFocus = true, focusRequester = serverUrlFocusRequester)
        FormField(S.fieldUsername, user, { user = it })
        FormField(S.fieldPassword, pass, { pass = it }, password = true)
    }
}

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
fun M3uDialog(onDismiss: () -> Unit, onSubmit: (name: String, url: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    val playlistUrlFocusRequester = remember { FocusRequester() }
    val S = com.ultratv.tv.nativeapp.i18n.LocalStrings.current
    AddProviderDialog(
        title = S.addProviderM3uTitle,
        onDismiss = onDismiss,
        onSubmit = { onSubmit(name, url) },
        canSubmit = url.isNotBlank(),
        initialFocusRequester = playlistUrlFocusRequester,
    ) {
        FormField(S.fieldNameOptional, name, { name = it })
        FormField(S.fieldPlaylistUrl, url, { url = it }, keyboardType = KeyboardType.Uri,
            placeholder = "https://host.tld/playlist.m3u", autoFocus = true, focusRequester = playlistUrlFocusRequester)
    }
}

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
fun StalkerDialog(onDismiss: () -> Unit, onSubmit: (name: String, url: String, mac: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var mac by remember { mutableStateOf("") }
    val portalUrlFocusRequester = remember { FocusRequester() }
    val S = com.ultratv.tv.nativeapp.i18n.LocalStrings.current
    AddProviderDialog(
        title = S.addProviderStalkerTitle,
        onDismiss = onDismiss,
        onSubmit = { onSubmit(name, url, mac) },
        canSubmit = url.isNotBlank() && mac.length in 12..17,
        initialFocusRequester = portalUrlFocusRequester,
    ) {
        FormField(S.fieldNameOptional, name, { name = it })
        FormField(S.fieldPortalUrl, url, { url = it }, keyboardType = KeyboardType.Uri,
            placeholder = "http://host:8080", autoFocus = true, focusRequester = portalUrlFocusRequester)
        FormField(S.fieldDeviceMac, mac, { mac = it.uppercase() },
            placeholder = "00:1A:79:XX:XX:XX")
    }
}

@Composable
private fun Modifier.androidx_border(focused: Boolean): Modifier = this.border(
    width = 1.dp,
    color = if (focused) com.ultratv.tv.nativeapp.ui.theme.UltraTokens.Accent else com.ultratv.tv.nativeapp.ui.theme.UltraTokens.Line2,
    shape = RoundedCornerShape(8.dp),
)
