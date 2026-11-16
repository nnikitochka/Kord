package su.nnedition.kord.ui.screen.main.page.search.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import su.nnedition.kord.data.model.Track
import su.nnedition.kord.player.PlayerState
import su.nnedition.kord.ui.screen.main.page.search.SearchTrackGridItem
import kotlin.collections.chunked

@Composable
fun TracksSection(
    tracks: List<Track>,
    currentTrack: Track?,
    playerState: PlayerState,
    page: Int,
    onPageChange: (Int) -> Unit,
    onTrackClick: (Track) -> Unit
) {
    val pageSize = 6 // 3 items x 2 columns
    val maxPages = (tracks.size + pageSize - 1) / pageSize
    val safePage = page.coerceIn(0, maxOf(0, maxPages - 1))
    val displayedTracks = tracks.drop(safePage * pageSize).take(pageSize)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Треки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (maxPages > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onPageChange(safePage - 1) },
                        enabled = safePage > 0,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Предыдущая страница",
                            tint = if (safePage > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onPageChange(safePage + 1) },
                        enabled = safePage < maxPages - 1,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Следующая страница",
                            tint = if (safePage < maxPages - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 2 Columns of 3 items
        val pairs = displayedTracks.chunked(2)
        pairs.forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    val track = pair[0]
                    val isPlaying = currentTrack?.id == track.id && playerState == PlayerState.PLAYING
                    su.nnedition.kord.ui.screen.main.page.search.SearchTrackGridItem(
                        track = track,
                        isPlaying = isPlaying,
                        onClick = { onTrackClick(track) }
                    )
                }

                if (pair.size > 1) {
                    Box(modifier = Modifier.weight(1f)) {
                        val track = pair[1]
                        val isPlaying = currentTrack?.id == track.id && playerState == PlayerState.PLAYING
                        su.nnedition.kord.ui.screen.main.page.search.SearchTrackGridItem(
                            track = track,
                            isPlaying = isPlaying,
                            onClick = { onTrackClick(track) }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}