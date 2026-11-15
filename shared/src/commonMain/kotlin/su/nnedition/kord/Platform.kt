package su.nnedition.kord

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun Modifier.onHover(onEnter: () -> Unit, onExit: () -> Unit): Modifier