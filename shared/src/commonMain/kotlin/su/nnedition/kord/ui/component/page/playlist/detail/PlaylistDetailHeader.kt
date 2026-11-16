package su.nnedition.kord.ui.component.page.playlist.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import su.nnedition.kord.data.model.Playlist
import su.nnedition.kord.ui.component.image.CoverImage
import su.nnedition.kord.ui.component.page.playlist.PlaylistsPage.playlistGradients
import su.nnedition.kord.util.TimeFormatter

@Composable
fun PlaylistDetailHeader(
    playlist: Playlist,
    onPlayAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = remember(playlist.id) {
        val idx = (playlist.id.hashCode() and 0x7FFFFFFF) % playlistGradients.size
        playlistGradients[idx]
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        CoverImage(
            url = playlist.coverArtUrl,
            contentDescription = playlist.name,
            modifier = Modifier.size(160.dp),
            shape = RoundedCornerShape(16.dp),
            fallbackIcon = Icons.AutoMirrored.Filled.PlaylistPlay,
            iconSize = 64.dp
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "ПЛЕЙЛИСТ",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${playlist.owner ?: "Пользователь"} • ${playlist.songCount} треков • ${TimeFormatter.formatTotalDuration(playlist.durationSeconds)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPlayAll,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Слушать",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview
@Composable
private fun PlaylistDetailHeaderPreview() {
    PlaylistDetailHeader(
        playlist = Playlist(
            id = "preview_1",
            name = "Любимые треки",
            owner = "admin",
            songCount = 28,
            durationSeconds = 4820
        ),
        onPlayAll = {}
    )
}
