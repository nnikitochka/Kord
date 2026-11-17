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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kord.shared.generated.resources.Res
import kord.shared.generated.resources.visibility
import kord.shared.generated.resources.visibility_off
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import su.nnedition.kord.service.AuthService
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun ServerSelectScreen(
    onLoginSuccess: () -> Unit = {}
) {
    var protocol by remember { mutableStateOf("https://") }
    var protocolExpanded by remember { mutableStateOf(false) }
    val protocols = listOf("https://", "http://")

    var serverAddress by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isConnecting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var pingState by remember { mutableStateOf(ServerPingState.IDLE) }
    val coroutineScope = rememberCoroutineScope()
    val authService = remember { AuthService.default }
    val logger = remember { Logger.withTag("ServerSelectScreen") }

    LaunchedEffect(protocol, serverAddress) {
        val trimmed = serverAddress.trim()
        if (trimmed.isEmpty()) {
            pingState = ServerPingState.IDLE
            return@LaunchedEffect
        }

        delay(400.milliseconds)

        pingState = ServerPingState.CHECKING

        try {
            val targetBase = "$protocol$trimmed".trimEnd('/')
            val isReachable = authService.ping(targetBase)
            pingState = if (isReachable) ServerPingState.SUCCESS else ServerPingState.FAILED
        } catch (ce: kotlinx.coroutines.CancellationException) {
            throw ce
        } catch (t: Throwable) {
            logger.w(t) { "Ошибка пинга: ${t.message}" }
            pingState = ServerPingState.FAILED
        }
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
                        OutlinedTextField(
                            value = protocol,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Протокол") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = protocolExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .height(64.dp),
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

                    OutlinedTextField(
                        value = serverAddress,
                        onValueChange = { input ->
                            errorMessage = null
                            val trimmed = input.trim()
                            if (trimmed.startsWith("https://", ignoreCase = true)) {
                                protocol = "https://"
                                serverAddress = trimmed.substring(8)
                            } else if (trimmed.startsWith("http://", ignoreCase = true)) {
                                protocol = "http://"
                                serverAddress = trimmed.substring(7)
                            } else {
                                if (protocol == "https://" && (trimmed.startsWith("192.168.") || trimmed.startsWith("10.") || trimmed.startsWith("127.") || trimmed.startsWith("localhost"))) {
                                    protocol = "http://"
                                }
                                serverAddress = input
                            }
                        },
                        label = { Text("Адрес сервера") },
                        placeholder = { Text("your-server.com") },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
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

                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        errorMessage = null
                    },
                    label = { Text("Пользователь") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Пароль") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
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
                                isConnecting = true
                                errorMessage = null
                                logger.i { "Нажата кнопка 'Подключиться': $targetBase для пользователя '$username'" }
                                coroutineScope.launch {
                                    try {
                                        val result = authService.login(
                                            baseUrl = targetBase,
                                            username = username,
                                            password = password
                                        )
                                        result.onSuccess { session ->
                                            logger.i { "Авторизация прошла успешно! Переход к главному экрану..." }
                                            onLoginSuccess()
                                        }.onFailure { ex ->
                                            logger.e(ex) { "Ошибка входа: ${ex.message}" }
                                            errorMessage = ex.message ?: "Ошибка авторизации"
                                        }
                                    } catch (t: Throwable) {
                                        logger.e(t) { "Неожиданный сбой: ${t.message}" }
                                        errorMessage = t.message ?: "Неожиданная ошибка"
                                    } finally {
                                        isConnecting = false
                                    }
                                }
                            },
                            enabled = !isConnecting && username.isNotBlank() && password.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Подключиться")
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
