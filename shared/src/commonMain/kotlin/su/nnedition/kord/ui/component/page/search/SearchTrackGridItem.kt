package su.nnedition.kord.ui.component.page.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import su.nnedition.kord.data.model.Track
import su.nnedition.kord.ui.component.image.CoverImage
import su.nnedition.kord.util.TimeFormatter

@Composable
fun SearchTrackGridItem(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isFavorite by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isPlaying -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 150)
    )

    val defaultGradient = remember(track.id) {
        val hues = listOf(
            listOf(Color(0xFF3182CE), Color(0xFF63B3ED)),
            listOf(Color(0xFFDD6B20), Color(0xFFF6AD55)),
            listOf(Color(0xFF38A169), Color(0xFF68D391)),
            listOf(Color(0xFF805AD5), Color(0xFFB794F4))
        )
        hues[(track.id.hashCode() and 0x7FFFFFFF) % hues.size]
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track Cover
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            CoverImage(
                url = track.coverArtUrl,
                contentDescription = track.title,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(6.dp),
                accentColor = track.accentColor,
                fallbackIcon = Icons.Default.MusicNote,
                iconSize = 20.dp
            )

            if (isHovered || isPlaying) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Играет" else "Воспроизвести",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Title and Artist
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
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

        Spacer(modifier = Modifier.width(8.dp))

        // Favorite icon
        IconButton(
            onClick = { isFavorite = !isFavorite },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "В избранное",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Duration
        Text(
            text = TimeFormatter.formatTrackDuration(track.durationSeconds),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(12.dp)
        )
    }
}

@Preview
@Composable
private fun SearchTrackGridItemPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        SearchTrackGridItem(
            track = Track(
                id = "1",
                title = "42",
                artist = "5opka, 6055",
                album = "NEXT LEVEL",
                durationSeconds = 102
            ),
            isPlaying = false,
            onClick = {}
        )
    }
}
