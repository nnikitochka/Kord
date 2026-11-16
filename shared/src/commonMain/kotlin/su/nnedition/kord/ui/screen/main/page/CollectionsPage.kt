package su.nnedition.kord.ui.screen.main.page

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import su.nnedition.kord.data.model.StarredCollection
import su.nnedition.kord.data.model.Track
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.player.PlayerManager
import su.nnedition.kord.ui.screen.common.Page
import su.nnedition.kord.ui.screen.main.page.collections.CollectionsCategory
import su.nnedition.kord.ui.screen.main.page.collections.CollectionsEmptyView
import su.nnedition.kord.ui.screen.main.page.collections.CollectionsTrackItem
import su.nnedition.kord.ui.screen.main.page.detail.AlbumDetailsPage
import su.nnedition.kord.ui.screen.main.page.detail.ArtistDetailsPage
import su.nnedition.kord.ui.screen.main.page.search.SearchAlbumCard
import su.nnedition.kord.ui.screen.main.page.search.SearchArtistCard

object CollectionsPage : Page {
    override val title: String = "Коллекции"
    override val icon: ImageVector = Icons.Default.LibraryMusic

    @Composable
    override fun Content(modifier: Modifier) {
        val client = remember { KordClient.default }
        val coroutineScope = rememberCoroutineScope()
        val player = remember { PlayerManager.instance }

        val currentTrack by player.currentTrack.collectAsState()

        var selectedAlbumId by remember { mutableStateOf<String?>(null) }
        var selectedArtistId by remember { mutableStateOf<String?>(null) }

        var selectedCategory by remember { mutableStateOf(CollectionsCategory.TRACKS) }
        var starred by remember { mutableStateOf(StarredCollection()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        fun loadStarred() {
            isLoading = true
            errorMessage = null
            coroutineScope.launch {
                client.getStarred()
                    .onSuccess { data ->
                        starred = data
                        isLoading = false
                    }
                    .onFailure { ex ->
                        errorMessage = ex.message ?: "Не удалось загрузить избранное"
                        isLoading = false
                    }
            }
        }

        fun toggleTrackFavorite(track: Track) {
            val wasStarred = track.isStarred
            val updatedTracks = if (wasStarred) {
                starred.tracks.filter { it.id != track.id }
            } else {
                starred.tracks.map { if (it.id == track.id) it.copy(isStarred = true) else it }
            }
            val previous = starred
            starred = starred.copy(tracks = updatedTracks)

            coroutineScope.launch {
                val res = if (wasStarred) {
                    client.unstar(trackIds = listOf(track.id))
                } else {
                    client.star(trackIds = listOf(track.id))
                }
                if (res.isFailure) {
                    starred = previous
                }
            }
        }

        LaunchedEffect(Unit) {
            loadStarred()
        }

        Crossfade(
            targetState = Triple(selectedAlbumId, selectedArtistId, isLoading),
            modifier = modifier.fillMaxSize()
        ) { (albumId, artistId, loading) ->
            when {
                albumId != null -> {
                    AlbumDetailsPage(
                        albumId = albumId,
                        onBack = { selectedAlbumId = null },
                        onArtistClick = { newArtistId ->
                            selectedArtistId = newArtistId
                            selectedAlbumId = null
                        }
                    )
                }

                artistId != null -> {
                    ArtistDetailsPage(
                        artistId = artistId,
                        onBack = { selectedArtistId = null },
                        onAlbumClick = { album ->
                            selectedAlbumId = album.id
                        }
                    )
                }

                loading -> {
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
                            Button(onClick = { loadStarred() }) {
                                Text("Повторить")
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Title
                        Text(
                            text = "Избранное",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Filter Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CollectionsCategory.entries.forEach { category ->
                                val count = when (category) {
                                    CollectionsCategory.TRACKS -> starred.tracks.size
                                    CollectionsCategory.ALBUMS -> starred.albums.size
                                    CollectionsCategory.ARTISTS -> starred.artists.size
                                }
                                val label = "${category.title} ($count)"
                                val isSelected = selectedCategory == category

                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Content by Category
                        when (selectedCategory) {
                            CollectionsCategory.TRACKS -> {
                                if (starred.tracks.isEmpty()) {
                                    CollectionsEmptyView(
                                        title = "Нет любимых треков",
                                        subtitle = "Нажимайте на сердечко у треков в приложении, чтобы они появлялись здесь"
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Action Header
                                        item {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 12.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        player.setQueue(
                                                            tracks = starred.tracks,
                                                            currentTrack = starred.tracks.first(),
                                                            autoPlay = true
                                                        )
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
                                                    Text("Слушать всё", fontWeight = FontWeight.SemiBold)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        val shuffled = starred.tracks.shuffled()
                                                        player.setQueue(
                                                            tracks = shuffled,
                                                            currentTrack = shuffled.first(),
                                                            autoPlay = true
                                                        )
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
                                            }
                                        }

                                        // Tracks
                                        itemsIndexed(
                                            items = starred.tracks,
                                            key = { _, track -> track.id }
                                        ) { index, track ->
                                            CollectionsTrackItem(
                                                track = track,
                                                index = index + 1,
                                                isPlaying = currentTrack?.id == track.id,
                                                onClick = {
                                                    player.setQueue(
                                                        tracks = starred.tracks,
                                                        currentTrack = track,
                                                        autoPlay = true
                                                    )
                                                },
                                                onToggleFavorite = {
                                                    toggleTrackFavorite(track)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            CollectionsCategory.ALBUMS -> {
                                if (starred.albums.isEmpty()) {
                                    CollectionsEmptyView(
                                        title = "Нет любимых альбомов",
                                        subtitle = "Нажимайте на сердечко на странице альбома, чтобы сохранить его в избранное"
                                    )
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 140.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(bottom = 16.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(
                                            items = starred.albums,
                                            key = { it.id }
                                        ) { album ->
                                            SearchAlbumCard(
                                                album = album,
                                                onClick = {
                                                    selectedAlbumId = album.id
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            CollectionsCategory.ARTISTS -> {
                                if (starred.artists.isEmpty()) {
                                    CollectionsEmptyView(
                                        title = "Нет любимых исполнителей",
                                        subtitle = "Отмечайте артистов сердечком, чтобы иметь быстрый доступ к их дискографии"
                                    )
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 130.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(bottom = 16.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(
                                            items = starred.artists,
                                            key = { it.id }
                                        ) { artist ->
                                            SearchArtistCard(
                                                artist = artist,
                                                onClick = {
                                                    selectedArtistId = artist.id
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
