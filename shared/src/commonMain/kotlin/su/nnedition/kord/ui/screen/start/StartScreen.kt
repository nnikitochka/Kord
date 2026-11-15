package su.nnedition.kord.ui.screen.start

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import kord.shared.generated.resources.Res
import kord.shared.generated.resources.info
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.painterResource
import su.nnedition.kord.onHover
import su.nnedition.kord.ui.theme.scaledDp

@OptIn(InternalResourceApi::class)
@Composable
fun StartScreen() {
    var selectedMode by remember { mutableStateOf<Mode?>(null) }
    val nextButtonAlpha by animateFloatAsState(
        targetValue = if (selectedMode != null) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )
    val nextButtonOffset by animateFloatAsState(
        targetValue = if (selectedMode != null) 0f else -20f,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = Modifier
            .background(Color(0xFF000000))
            .safeContentPadding()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.animateContentSize(
                animationSpec = tween(durationMillis = 300)
            ),
            colors = CardColors(
                containerColor = Color(0xFF121212),
                contentColor = CardDefaults.cardColors().contentColor,
                disabledContainerColor = CardDefaults.cardColors().disabledContainerColor,
                disabledContentColor = CardDefaults.cardColors().disabledContentColor
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Выберите режим работы")
                    Spacer(modifier = Modifier.width(3.scaledDp))
                    InfoIcon(text = "Онлайн режим подключается к серверу,\nАвтономный работает локально")
                }

                Spacer(modifier = Modifier.height(6.dp))

                ModeButton(Mode.ONLINE, selectedMode == Mode.ONLINE) {
                    selectedMode = Mode.ONLINE
                }

                ModeButton(Mode.OFFLINE, selectedMode == Mode.OFFLINE) {
                    selectedMode = Mode.OFFLINE
                }

                if (selectedMode != null || nextButtonAlpha > 0f) {
                    Spacer(modifier = Modifier.height(6.dp))

                    NextButton(
                        onClick = { selectedMode = null },
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = nextButtonAlpha
                                translationY = nextButtonOffset.dp.toPx()
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun NextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor by animateColorAsState(
        targetValue = if (isHovered) Color(0xFFCCCC00) else Color(0xFFffff00),
        animationSpec = tween(durationMillis = 150)
    )

    Box(
        modifier = modifier
            .size(230.scaledDp, 40.scaledDp)
            .background(color = backgroundColor, shape = RoundedCornerShape(5.dp))
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Далее",
            fontSize = TextUnit(15f, TextUnitType.Sp),
            color = Color(0xFF121212)
        )
    }
}

@OptIn(InternalResourceApi::class)
@Composable
fun InfoIcon(text: String) {
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .onHover(
                onEnter = { isVisible = true },
                onExit = { isVisible = false }
            )
    ) {
        Icon(
            painter = painterResource(Res.drawable.info),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFFffff00)
        )

        if (isVisible || alpha > 0f) {
            Popup(
                popupPositionProvider = object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        return IntOffset(
                            x = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2 - popupContentSize.width / 2,
                            y = anchorBounds.bottom
                        )
                    }
                },
                properties = PopupProperties(focusable = false)
            ) {
                Box(
                    modifier = Modifier
                        .onHover(
                            onEnter = { isVisible = true },
                            onExit = { isVisible = false }
                        )
                        .graphicsLayer { this.alpha = alpha }
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF404040), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .widthIn(max = 220.dp)
                ) {
                    Text(
                        text = text,
                        fontSize = TextUnit(13f, TextUnitType.Sp),
                        color = Color(0xFFCCCCCC),
                        lineHeight = TextUnit(18f, TextUnitType.Sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    mode: Mode,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor by animateColorAsState(
        targetValue = when {
            selected -> Color(0xFFffff00)
            isHovered -> Color(0xFF1E1E1E)
            else -> Color(0xFF121212)
        },
        animationSpec = tween(durationMillis = 150)
    )

    Box(
        modifier = Modifier
            .size(230.scaledDp, 40.scaledDp)
            .background(color = backgroundColor, shape = RoundedCornerShape(5.dp))
            .then(
                if (!selected) Modifier.border(1.dp, Color(0xFF808080), RoundedCornerShape(5.dp))
                else Modifier
            )
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelect
            )
            .padding(horizontal = 12.scaledDp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = mode.description,
            fontSize = TextUnit(15f, TextUnitType.Sp),
            color = if (selected) Color(0xFF121212) else Color(0xFFffff00)
        )
    }
}

private enum class Mode(
    val description: String
) {
    ONLINE("Онлайн режим"),
    OFFLINE("Автономный режим"),
}