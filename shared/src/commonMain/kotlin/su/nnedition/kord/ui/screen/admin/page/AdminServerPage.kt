package su.nnedition.kord.ui.screen.admin.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import su.nnedition.kord.data.model.NowPlayingItem
import su.nnedition.kord.data.model.ServerHealthInfo
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.ui.screen.common.Page

object AdminServerPage : Page {
    override val title: String = "Сервер"
    override val icon: ImageVector = Icons.Default.Dns

    @Composable
    override fun Content(modifier: Modifier) {
        val scope = rememberCoroutineScope()
        val client = KordClient.default
        val config = client.getConfig()

        var isPinging by remember { mutableStateOf(false) }
        var isOnline by remember { mutableStateOf<Boolean?>(null) }
        var serverHealth by remember { mutableStateOf<ServerHealthInfo?>(null) }
        var nowPlayingList by remember { mutableStateOf<List<NowPlayingItem>>(emptyList()) }

        fun loadTelemetry() {
            scope.launch {
                isPinging = true
                isOnline = client.ping()
                client.getServerHealth().onSuccess { serverHealth = it }
                client.getNowPlaying().onSuccess { nowPlayingList = it }
                isPinging = false
            }
        }

        LaunchedEffect(Unit) {
            loadTelemetry()
            while (isActive) {
                delay(10000L)
                client.getNowPlaying().onSuccess { nowPlayingList = it }
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Состояние сервера",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Параметры соединения, диагностика компонентов и активные стримы",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { loadTelemetry() }, enabled = !isPinging) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Проверить статус")
                }
            }

            // Основная карточка соединения
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (isOnline) {
                                            true -> Color(0xFF4CAF50)
                                            false -> MaterialTheme.colorScheme.error
                                            null -> Color.Gray
                                        }
                                    )
                            )
                            Text(
                                text = when (isOnline) {
                                    true -> "Сервер онлайн"
                                    false -> "Сервер недоступен"
                                    null -> "Проверка соединения..."
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (isPinging) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ServerParamRow("Адрес сервера", config.baseUrl.ifEmpty { "Не настроен" })
                        ServerParamRow("Текущий пользователь", config.username.ifEmpty { "Не авторизован" })
                        ServerParamRow("Протокол API", "Subsonic / Kordinator API v1.15.0")
                        ServerParamRow("Формат ответов", "JSON (REST)")
                        ServerParamRow("Клиентское приложение", "Kord Compose Multiplatform")
                    }

                    Spacer(modifier = Modifier.size(4.dp))

                    Button(
                        onClick = { loadTelemetry() },
                        enabled = !isPinging
                    ) {
                        Text("Повторить пинг и обновить данные")
                    }
                }
            }

            // Карточка здоровья компонентов
            ServerHealthCard(health = serverHealth)

            // Карточка "Сейчас слушают"
            NowPlayingCard(items = nowPlayingList)
        }
    }
}

@Preview
@Composable
private fun AdminServerPagePreview() {
    AdminServerPage.Content(modifier = Modifier.fillMaxSize())
}
