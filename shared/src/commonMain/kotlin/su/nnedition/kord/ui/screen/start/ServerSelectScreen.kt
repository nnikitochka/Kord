package su.nnedition.kord.ui.screen.start

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kord.shared.generated.resources.Res
import kord.shared.generated.resources.visibility
import kord.shared.generated.resources.visibility_off
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.data.remote.ServerConfig

enum class ServerPingState {
    IDLE,
    CHECKING,
    SUCCESS,
    FAILED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun ServerSelectScreen() {
    var protocol by remember { mutableStateOf("https://") }
    var protocolExpanded by remember { mutableStateOf(false) }
    val protocols = listOf("https://", "http://")

    var serverAddress by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var pingState by remember { mutableStateOf(ServerPingState.IDLE) }
    val kordClient = remember { KordClient.default }

    LaunchedEffect(protocol, serverAddress) {
        val trimmed = serverAddress.trim()
        if (trimmed.isEmpty()) {
            pingState = ServerPingState.IDLE
            return@LaunchedEffect
        }

        // Быстрый дебаунс при вводе
        delay(250.milliseconds)

        pingState = ServerPingState.CHECKING

        val targetBase = "$protocol$trimmed".trimEnd('/')
        val isReachable = kordClient.ping(targetBase)

        pingState = if (isReachable) ServerPingState.SUCCESS else ServerPingState.FAILED
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(10.dp),
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Подключение к серверу",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = protocolExpanded,
                        onExpandedChange = { protocolExpanded = it },
                        modifier = Modifier.width(120.dp)
                    ) {
                        KordOutlinedTextField(
                            value = protocol,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Протокол") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = protocolExpanded)
                            },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            contentPadding = PaddingValues(start = 10.dp, top = 2.dp, end = 4.dp, bottom = 4.dp),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = protocolExpanded,
                            onDismissRequest = { protocolExpanded = false }
                        ) {
                            protocols.forEach { proto ->
                                DropdownMenuItem(
                                    text = { Text(proto) },
                                    onClick = {
                                        protocol = proto
                                        protocolExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    KordOutlinedTextField(
                        value = serverAddress,
                        onValueChange = { input ->
                            if (input.startsWith("https://", ignoreCase = true)) {
                                protocol = "https://"
                                serverAddress = input.substring(8)
                            } else if (input.startsWith("http://", ignoreCase = true)) {
                                protocol = "http://"
                                serverAddress = input.substring(7)
                            } else {
                                serverAddress = input
                            }
                        },
                        label = { Text("Адрес сервера") },
                        placeholder = { Text("your-server.com") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 12.dp, top = 2.dp, end = 12.dp, bottom = 4.dp),
                        singleLine = true,
                        trailingIcon = {
                            when (pingState) {
                                ServerPingState.CHECKING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                ServerPingState.SUCCESS -> {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color(0xFF4CAF50), shape = CircleShape)
                                    )
                                }
                                ServerPingState.FAILED -> {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(MaterialTheme.colorScheme.error, shape = CircleShape)
                                    )
                                }
                                ServerPingState.IDLE -> {}
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                KordOutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 12.dp, top = 2.dp, end = 12.dp, bottom = 4.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                KordOutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 12.dp, top = 2.dp, end = 12.dp, bottom = 4.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation('*'),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (passwordVisible) Res.drawable.visibility_off else Res.drawable.visibility
                                ),
                                contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                AnimatedVisibility(
                    visible = pingState == ServerPingState.SUCCESS,
                    enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                            expandVertically(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing), expandFrom = Alignment.Top),
                    exit = fadeOut(animationSpec = tween(durationMillis = 100)) +
                           shrinkVertically(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing), shrinkTowards = Alignment.Top),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val targetBase = "$protocol${serverAddress.trim()}".trimEnd('/')
                                kordClient.updateConfig(
                                    ServerConfig(
                                        baseUrl = targetBase,
                                        username = username,
                                        password = password
                                    )
                                )
                            },
                            enabled = username.isNotBlank() && password.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Подключиться")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KordOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentPadding: PaddingValues = PaddingValues(start = 12.dp, top = 2.dp, end = 12.dp, bottom = 4.dp)
) {
    val colors = OutlinedTextFieldDefaults.colors()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val textColor = textStyle.color.takeOrElse {
        if (isError) colors.errorTextColor
        else if (!enabled) colors.disabledTextColor
        else if (isFocused) colors.focusedTextColor
        else colors.unfocusedTextColor
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    BasicTextField(
        value = value,
        modifier = modifier.height(60.dp),
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(if (isError) colors.errorCursorColor else colors.cursorColor),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        decorationBox = @Composable { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                contentPadding = contentPadding
            )
        }
    )
}

