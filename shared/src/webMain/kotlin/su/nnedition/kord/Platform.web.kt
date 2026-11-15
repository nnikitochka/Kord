package su.nnedition.kord

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
@androidx.compose.runtime.Composable
actual fun Modifier.onHover(
    onEnter: () -> Unit,
    onExit: () -> Unit
): Modifier =
    this.onPointerEvent(PointerEventType.Enter) { onEnter() }
        .onPointerEvent(PointerEventType.Exit) { onExit() }