package su.nnedition.kord.ui.component.page.playlist.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import su.nnedition.kord.data.model.Track
import su.nnedition.kord.util.TimeFormatter

@Composable
fun PlaylistTrackItem(
    track: Track,
    index: Int,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val highlightColor by animateColorAsState(
        targetValue = when {
            isPressed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            isHovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(highlightColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isPlaying -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Играет",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    isHovered -> {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Воспроизвести",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = index.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = track.album,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            Text(
                text = TimeFormatter.formatTrackDuration(track.durationSeconds),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun PlaylistTrackItemPreview() {
    PlaylistTrackItem(
        track = Track(
            id = "1",
            title = "А река течёт",
            artist = "Любэ",
            artistId = "10",
            album = "Своё",
            albumId = "20",
            durationSeconds = 245,
            trackNumber = 1,
            discNumber = 1,
            year = 2026,
            genre = "Rock",
            coverArtUrl = null,
            streamUrl = "",
            bitRate = 320,
            contentType = "audio/flac"
        ),
        index = 1,
        onClick = {}
    )
}
