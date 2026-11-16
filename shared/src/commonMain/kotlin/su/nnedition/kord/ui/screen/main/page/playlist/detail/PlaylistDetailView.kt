package su.nnedition.kord.ui.screen.main.page.playlist.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import su.nnedition.kord.data.model.Playlist
import su.nnedition.kord.data.model.Track
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.player.PlayerManager
import su.nnedition.kord.player.PlayerState

@Composable
fun PlaylistDetailView(
    playlist: Playlist,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val client = remember { KordClient.default }
    val coroutineScope = rememberCoroutineScope()
    val player = remember { PlayerManager.instance }

    val currentTrack by player.currentTrack.collectAsState()
    val playerState by player.state.collectAsState()

    var detailedPlaylist by remember(playlist.id) { mutableStateOf<Playlist?>(null) }
    var isLoading by remember(playlist.id) { mutableStateOf(true) }
    var errorMessage by remember(playlist.id) { mutableStateOf<String?>(null) }

    fun loadPlaylist() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            client.getPlaylist(playlist.id)
                .onSuccess { detailed ->
                    detailedPlaylist = detailed
                    isLoading = false
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "Не удалось загрузить треки"
                    isLoading = false
                }
        }
    }

    LaunchedEffect(playlist.id) {
        loadPlaylist()
    }

    val currentPlaylist = detailedPlaylist ?: playlist
    val tracks = detailedPlaylist?.tracks ?: emptyList()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        su.nnedition.kord.ui.screen.main.page.playlist.detail.PlaylistDetailHeader(
            playlist = currentPlaylist,
            onPlayAll = {
                if (tracks.isNotEmpty()) {
                    player.setQueue(tracks = tracks, currentTrack = tracks.first(), autoPlay = true)
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tracks table header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "#",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(32.dp)
            )
            Text(
                text = "НАЗВАНИЕ",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1.5f)
                    .padding(horizontal = 8.dp)
            )
            Text(
                text = "АЛЬБОМ",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Длительность",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(16.dp)
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "Произошла ошибка",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = { loadPlaylist() }) {
                            Text("Повторить")
                        }
                    }
                }
            }

            tracks.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "В этом плейлисте пока нет треков",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                        val isPlaying = currentTrack?.id == track.id && playerState == PlayerState.PLAYING
                        su.nnedition.kord.ui.screen.main.page.playlist.detail.PlaylistTrackItem(
                            track = track,
                            index = index + 1,
                            isPlaying = isPlaying,
                            onClick = {
                                if (currentTrack?.id == track.id) {
                                    player.togglePlayPause()
                                } else {
                                    player.setQueue(
                                        tracks = tracks,
                                        currentTrack = track,
                                        autoPlay = true
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlaylistDetailViewPreview() {
    su.nnedition.kord.ui.screen.main.page.playlist.detail.PlaylistDetailView(
        playlist = Playlist(
            id = "preview_1",
            name = "Любимые треки",
            owner = "admin",
            songCount = 2,
            durationSeconds = 480,
            tracks = listOf(
                Track(
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
                Track(
                    id = "2",
                    title = "Позови меня тихо по имени",
                    artist = "Любэ",
                    artistId = "10",
                    album = "Комбат",
                    albumId = "21",
                    durationSeconds = 235,
                    trackNumber = 2,
                    discNumber = 1,
                    year = 2026,
                    genre = "Rock",
                    coverArtUrl = null,
                    streamUrl = "",
                    bitRate = 320,
                    contentType = "audio/flac"
                )
            )
        ),
        onBack = {}
    )
}
