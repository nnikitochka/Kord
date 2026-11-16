package su.nnedition.kord.ui.component.page.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import su.nnedition.kord.data.model.Album
import su.nnedition.kord.ui.component.image.CoverImage

@Composable
fun SearchAlbumCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val cardBackgroundColor by animateColorAsState(
        targetValue = if (isHovered) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 150)
    )

    val defaultGradient = remember(album.id) {
        val hues = listOf(
            listOf(Color(0xFF2B6CB0), Color(0xFF4299E1)),
            listOf(Color(0xFFC53030), Color(0xFFF56565)),
            listOf(Color(0xFF2F855A), Color(0xFF48BB78)),
            listOf(Color(0xFF6B46C1), Color(0xFF9F7AEA)),
            listOf(Color(0xFFD69E2E), Color(0xFFECC94B))
        )
        hues[(album.id.hashCode() and 0x7FFFFFFF) % hues.size]
    }

    Column(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBackgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier.size(124.dp),
            contentAlignment = Alignment.Center
        ) {
            CoverImage(
                url = album.coverArtUrl,
                contentDescription = album.title,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                accentColor = album.accentColor,
                fallbackIcon = Icons.Default.Album,
                iconSize = 48.dp
            )

            if (isHovered) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Воспроизвести альбом",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val subtitle = buildString {
            append(album.artist)
            if (album.year != null) {
                append(" • ")
                append(album.year)
            }
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun SearchAlbumCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        SearchAlbumCard(
            album = Album(
                id = "1",
                title = "NEXT LEVEL",
                artist = "Neverlove",
                year = 2024
            ),
            onClick = {}
        )
    }
}
