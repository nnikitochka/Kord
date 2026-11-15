package su.nnedition.kord.ui.screen.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import su.nnedition.kord.player.PlayerManager
import su.nnedition.kord.ui.component.MiniPlayer
import su.nnedition.kord.ui.component.page.CollectionsPage
import su.nnedition.kord.ui.component.page.HomePage
import su.nnedition.kord.ui.component.page.Page
import su.nnedition.kord.ui.component.page.playlist.PlaylistsPage
import su.nnedition.kord.ui.component.page.SearchPage
import su.nnedition.kord.ui.component.page.SettingsPage

@Composable
@Preview
fun MainViewScreen(
    topPages: List<Page> = listOf(HomePage, SearchPage, PlaylistsPage, CollectionsPage),
    bottomPages: List<Page> = listOf(SettingsPage)
) {
    var selectedPage by remember { mutableStateOf(topPages.firstOrNull() ?: HomePage) }

    LaunchedEffect(Unit) {
        if (PlayerManager.instance.currentTrack.value == null) {
            PlayerManager.restoreQueue()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                topPages.forEach { page ->
                    PageNavButton(
                        page = page,
                        isSelected = selectedPage == page,
                        onClick = { selectedPage = page }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                bottomPages.forEach { page ->
                    PageNavButton(
                        page = page,
                        isSelected = selectedPage == page,
                        onClick = { selectedPage = page }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(targetState = selectedPage) { page ->
                    page.Content(modifier = Modifier.fillMaxSize())
                }
            }

            MiniPlayer(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PageNavButton(
    page: Page,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            isPressed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            isHovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )

    val iconTint by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isHovered -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = page.title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}
