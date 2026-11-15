package su.nnedition.kord.ui

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import su.nnedition.kord.player.PlayerManager
import su.nnedition.kord.service.AuthService
import su.nnedition.kord.ui.navigation.AppNavigator
import su.nnedition.kord.ui.navigation.MainRoute
import su.nnedition.kord.ui.navigation.ServerSelectRoute
import su.nnedition.kord.ui.screen.main.MainViewScreen
import su.nnedition.kord.ui.screen.start.ServerSelectScreen

@Composable
fun KordApp(
    navigator: AppNavigator = remember { AppNavigator.default },
    authService: AuthService = remember { AuthService.default }
) {
    LaunchedEffect(Unit) {
        val logger = Logger.withTag("KordApp")
        logger.i { "Инициализация приложения, проверка сохранённой сессии..." }
        val session = authService.restoreSession()
        if (session != null) {
            logger.i { "Найдена сохранённая сессия для '${session.username}'. Переход на MainRoute." }
            navigator.replace(MainRoute)
            PlayerManager.restoreQueue()
        } else {
            logger.i { "Сессия не найдена, открываем экран авторизации (ServerSelectRoute)." }
        }
    }

    val currentScreen by navigator.currentScreen.collectAsState()

    Crossfade(targetState = currentScreen) { screen ->
        when (screen) {
            ServerSelectRoute -> ServerSelectScreen(
                onLoginSuccess = {
                    navigator.replace(MainRoute)
                    PlayerManager.restoreQueue()
                }
            )
            MainRoute -> MainViewScreen()
        }
    }
}
