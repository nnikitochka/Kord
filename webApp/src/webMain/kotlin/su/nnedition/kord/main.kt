package su.nnedition.kord

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import su.nnedition.kord.ui.KordApp
import su.nnedition.kord.ui.theme.KordAppTheme

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        KordAppTheme {
            KordApp()
        }
    }
}