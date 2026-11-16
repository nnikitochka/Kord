package su.nnedition.kord.ui.screen.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import su.nnedition.kord.player.PlayerManager
import su.nnedition.kord.ui.component.MiniPlayer

@Composable
fun CommonViewScreen(
    topPages: List<Page>,
    bottomPages: List<Page> = emptyList(),
    beforeBottomPages: @Composable (() -> Unit)? = null,
    afterBottomPages: @Composable (() -> Unit)? = null,
) {
    var selectedPage by remember(topPages) {
        mutableStateOf(topPages.firstOrNull() ?: bottomPages.firstOrNull())
    }

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

                beforeBottomPages?.invoke()

                bottomPages.forEach { page ->
                    PageNavButton(
                        page = page,
                        isSelected = selectedPage == page,
                        onClick = { selectedPage = page }
                    )
                }

                afterBottomPages?.invoke()
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
                    if (page != null) {
                        page.Content(modifier = Modifier.fillMaxSize())
                    } else {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            MiniPlayer(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
