package su.nnedition.kord.ui.screen.main.page.detail

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import su.nnedition.kord.data.model.Album
import su.nnedition.kord.data.model.Track
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.player.PlayerManager
import su.nnedition.kord.ui.component.image.CoverImage
import su.nnedition.kord.ui.screen.main.page.detail.playlist.PlaylistTrackItem
import su.nnedition.kord.util.TimeFormatter

@Composable
fun AlbumDetailsPage(
    albumId: String,
    onBack: () -> Unit,
    onArtistClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val client = remember { KordClient.default }
    val coroutineScope = rememberCoroutineScope()
    val player = remember { PlayerManager.instance }

    val currentTrack by player.currentTrack.collectAsState()

    var album by remember(albumId) { mutableStateOf<Album?>(null) }
    var isLoading by remember(albumId) { mutableStateOf(true) }
    var errorMessage by remember(albumId) { mutableStateOf<String?>(null) }

    fun loadAlbum() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            client.getAlbum(albumId)
                .onSuccess { detailed ->
                    album = detailed
                    isLoading = false
                }
                .onFailure { ex ->
                    errorMessage = ex.message ?: "Не удалось загрузить альбом"
                    isLoading = false
                }
        }
    }

    fun toggleAlbumFavorite() {
        val current = album ?: return
        val newStarred = !current.isStarred
        album = current.copy(isStarred = newStarred)
        coroutineScope.launch {
            val result = if (newStarred) client.star(albumIds = listOf(current.id)) else client.unstar(albumIds = listOf(current.id))
            if (result.isFailure) {
                album = current
            }
        }
    }

    fun toggleTrackFavorite(track: Track) {
        val current = album ?: return
        val newStarred = !track.isStarred
        album = current.copy(
            tracks = current.tracks.map { if (it.id == track.id) it.copy(isStarred = newStarred) else it }
        )
        coroutineScope.launch {
            val result = if (newStarred) client.star(trackIds = listOf(track.id)) else client.unstar(trackIds = listOf(track.id))
            if (result.isFailure) {
                album = current
            }
        }
    }

    LaunchedEffect(albumId) {
        loadAlbum()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "Ошибка",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { loadAlbum() }) {
                            Text("Повторить")
                        }
                    }
                }
            }

            album != null -> {
                val currentAlbum = album!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Album Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            CoverImage(
                                url = currentAlbum.coverArtUrl,
                                contentDescription = currentAlbum.title,
                                modifier = Modifier.size(160.dp),
                                shape = RoundedCornerShape(16.dp),
                                fallbackIcon = Icons.Default.Album,
                                iconSize = 64.dp
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "АЛЬБОМ",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentAlbum.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentAlbum.artist,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (onArtistClick != null && currentAlbum.artistId != null) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = if (onArtistClick != null && currentAlbum.artistId != null) {
                                        Modifier.clickable { onArtistClick(currentAlbum.artistId) }
                                    } else Modifier
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val meta = buildString {
                                    currentAlbum.year?.let { append("$it • ") }
                                    currentAlbum.genre?.let { append("$it • ") }
                                    append("${currentAlbum.songCount} треков • ")
                                    append(TimeFormatter.formatTotalDuration(currentAlbum.durationSeconds))
                                }
                                Text(
                                    text = meta,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Play All
                                    Button(
                                        onClick = {
                                            if (currentAlbum.tracks.isNotEmpty()) {
                                                player.setQueue(
                                                    tracks = currentAlbum.tracks,
                                                    currentTrack = currentAlbum.tracks.first(),
                                                    autoPlay = true
                                                )
                                            }
                                        },
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

                                    // Shuffle
                                    OutlinedButton(
                                        onClick = {
                                            if (currentAlbum.tracks.isNotEmpty()) {
                                                val shuffled = currentAlbum.tracks.shuffled()
                                                player.setQueue(
                                                    tracks = shuffled,
                                                    currentTrack = shuffled.first(),
                                                    autoPlay = true
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shuffle,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Вперемешку")
                                    }

                                    // Album Favorite Button
                                    IconButton(
                                        onClick = { toggleAlbumFavorite() },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (currentAlbum.isStarred) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = if (currentAlbum.isStarred) "Удалить из избранного" else "В избранное",
                                            tint = if (currentAlbum.isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Table Header
                    item {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(32.dp)
                                )
                                Text(
                                    text = "НАЗВАНИЕ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1.5f).padding(horizontal = 8.dp)
                                )
                                Text(
                                    text = "ИСПОЛНИТЕЛЬ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Длительность",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                thickness = 1.dp
                            )
                        }
                    }

                    // Tracks List
                    itemsIndexed(
                        items = currentAlbum.tracks,
                        key = { _, track -> track.id }
                    ) { index, track ->
                        PlaylistTrackItem(
                            track = track,
                            index = index + 1,
                            isPlaying = currentTrack?.id == track.id,
                            onClick = {
                                player.setQueue(
                                    tracks = currentAlbum.tracks,
                                    currentTrack = track,
                                    autoPlay = true
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
