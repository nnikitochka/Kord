package su.nnedition.kord

import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.russhwolf.settings.Settings
import kord.shared.generated.resources.Res
import kord.shared.generated.resources.kord_logo
import org.jetbrains.compose.resources.painterResource
import su.nnedition.kord.ui.KordApp
import su.nnedition.kord.ui.theme.KordAppTheme

fun main() = application {
    val settings = remember { Settings() }
    val isMaximized = remember { settings.getBoolean("window_maximized", false) }
    val windowState = rememberWindowState(
        width = 1280.dp,
        height = 800.dp,
        position = WindowPosition.Aligned(Alignment.Center),
        placement = if (isMaximized) WindowPlacement.Maximized else WindowPlacement.Floating
    )

    Window(
        onCloseRequest = {
            settings.putBoolean("window_maximized", windowState.placement == WindowPlacement.Maximized)
            exitApplication()
        },
        state = windowState,
        title = "Kord",
        icon = painterResource(Res.drawable.kord_logo)
    ) {
        KordAppTheme {
            KordApp()
        }
    }
}