package su.nnedition.kord.ui.component.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import su.nnedition.kord.data.model.Album
import su.nnedition.kord.data.model.Artist
import su.nnedition.kord.data.model.SearchResult
import su.nnedition.kord.data.model.Track
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.player.PlayerManager
import su.nnedition.kord.player.PlayerState
import su.nnedition.kord.ui.component.page.search.SearchAlbumCard
import su.nnedition.kord.ui.component.page.search.SearchArtistCard
import su.nnedition.kord.ui.component.page.search.SearchCategory
import su.nnedition.kord.ui.component.page.search.SearchCategoryChips
import su.nnedition.kord.ui.component.page.search.SearchInputBar
import su.nnedition.kord.ui.component.page.search.SearchTrackGridItem

object SearchPage : Page {
    override val title: String = "Поиск"
    override val icon: ImageVector = Icons.Default.Search

    @Composable
    override fun Content(modifier: Modifier) {
        val client = remember { KordClient.default }
        val player = remember { PlayerManager.instance }

        val currentTrack by player.currentTrack.collectAsState()
        val playerState by player.state.collectAsState()

        var query by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf(SearchCategory.TOP) }
        var searchResult by remember { mutableStateOf<SearchResult?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var trackPage by remember { mutableIntStateOf(0) }

        // Live search with debounce
        LaunchedEffect(query) {
            trackPage = 0
            val clean = query.trim()
            if (clean.isBlank()) {
                searchResult = null
                isLoading = false
                return@LaunchedEffect
            }

            delay(350L)
            isLoading = true
            val result = client.search(clean, artistCount = 20, albumCount = 20, songCount = 30)
            result.onSuccess {
                searchResult = it
            }.onFailure {
                searchResult = SearchResult()
            }
            isLoading = false
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. Search Input Bar
            SearchInputBar(
                query = query,
                onQueryChange = { query = it },
                onClear = {
                    query = ""
                    searchResult = null
                }
            )

            // 2. Category Chips (Visible when query is not blank)
            AnimatedVisibility(
                visible = query.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    SearchCategoryChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Search Results Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    query.isBlank() -> {
                        // Empty query state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Text(
                                    text = "Найдите треки, альбомы и любимых исполнителей",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    searchResult != null && searchResult!!.tracks.isEmpty() && searchResult!!.albums.isEmpty() && searchResult!!.artists.isEmpty() -> {
                        // No results state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "По запросу «$query» ничего не найдено",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    searchResult != null -> {
                        val result = searchResult!!

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            when (selectedCategory) {
                                SearchCategory.TOP -> {
                                    // TOP: Tracks Block (2 columns)
                                    if (result.tracks.isNotEmpty()) {
                                        item {
                                            TracksSection(
                                                tracks = result.tracks,
                                                currentTrack = currentTrack,
                                                playerState = playerState,
                                                page = trackPage,
                                                onPageChange = { trackPage = it },
                                                onTrackClick = { track ->
                                                    player.setQueue(tracks = result.tracks, currentTrack = track, autoPlay = true)
                                                }
                                            )
                                        }
                                    }

                                    // Albums Block
                                    if (result.albums.isNotEmpty()) {
                                        item {
                                            AlbumsSection(
                                                albums = result.albums,
                                                onAlbumClick = { album ->
                                                    // Future: navigate to album or play
                                                }
                                            )
                                        }
                                    }

                                    // Artists Block
                                    if (result.artists.isNotEmpty()) {
                                        item {
                                            ArtistsSection(
                                                artists = result.artists,
                                                onArtistClick = { artist ->
                                                    // Future: navigate to artist
                                                }
                                            )
                                        }
                                    }
                                }

                                SearchCategory.TRACKS -> {
                                    item {
                                        Text(
                                            text = "Все треки (${result.tracks.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }

                                    itemsIndexed(result.tracks.chunked(2)) { _, pair ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                val track = pair[0]
                                                val isPlaying = currentTrack?.id == track.id && playerState == PlayerState.PLAYING
                                                SearchTrackGridItem(
                                                    track = track,
                                                    isPlaying = isPlaying,
                                                    onClick = {
                                                        player.setQueue(tracks = result.tracks, currentTrack = track, autoPlay = true)
                                                    }
                                                )
                                            }

                                            if (pair.size > 1) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    val track = pair[1]
                                                    val isPlaying = currentTrack?.id == track.id && playerState == PlayerState.PLAYING
                                                    SearchTrackGridItem(
                                                        track = track,
                                                        isPlaying = isPlaying,
                                                        onClick = {
                                                            player.setQueue(tracks = result.tracks, currentTrack = track, autoPlay = true)
                                                        }
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }

                                SearchCategory.ALBUMS -> {
                                    item {
                                        Text(
                                            text = "Все альбомы (${result.albums.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }

                                    items(result.albums.chunked(5)) { albumRow ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            albumRow.forEach { album ->
                                                SearchAlbumCard(
                                                    album = album,
                                                    onClick = {}
                                                )
                                            }
                                        }
                                    }
                                }

                                SearchCategory.ARTISTS -> {
                                    item {
                                        Text(
                                            text = "Все исполнители (${result.artists.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }

                                    items(result.artists.chunked(5)) { artistRow ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            artistRow.forEach { artist ->
                                                SearchArtistCard(
                                                    artist = artist,
                                                    onClick = {}
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
}

@Composable
private fun TracksSection(
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
                    SearchTrackGridItem(
                        track = track,
                        isPlaying = isPlaying,
                        onClick = { onTrackClick(track) }
                    )
                }

                if (pair.size > 1) {
                    Box(modifier = Modifier.weight(1f)) {
                        val track = pair[1]
                        val isPlaying = currentTrack?.id == track.id && playerState == PlayerState.PLAYING
                        SearchTrackGridItem(
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

@Composable
private fun AlbumsSection(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Альбомы",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            albums.forEach { album ->
                SearchAlbumCard(
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }
        }
    }
}

@Composable
private fun ArtistsSection(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Исполнители",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            artists.forEach { artist ->
                SearchArtistCard(
                    artist = artist,
                    onClick = { onArtistClick(artist) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun SearchPagePreview() {
    SearchPage.Content(modifier = Modifier.fillMaxSize())
}
