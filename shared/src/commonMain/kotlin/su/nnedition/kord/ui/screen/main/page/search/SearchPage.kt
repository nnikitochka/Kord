package su.nnedition.kord.ui.screen.main.page.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import su.nnedition.kord.data.model.SearchResult
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.player.PlayerManager
import su.nnedition.kord.player.PlayerState
import su.nnedition.kord.ui.screen.common.Page
import su.nnedition.kord.ui.screen.main.page.search.section.AlbumsSection
import su.nnedition.kord.ui.screen.main.page.search.section.ArtistsSection
import su.nnedition.kord.ui.screen.main.page.search.section.TracksSection
import kotlin.time.Duration.Companion.milliseconds

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
        var selectedCategory by remember { mutableStateOf(su.nnedition.kord.ui.screen.main.page.search.SearchCategory.TOP) }
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

            delay(350L.milliseconds)
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
            su.nnedition.kord.ui.screen.main.page.search.SearchInputBar(
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
                    su.nnedition.kord.ui.screen.main.page.search.SearchCategoryChips(
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
                                su.nnedition.kord.ui.screen.main.page.search.SearchCategory.TOP -> {
                                    // TOP: Tracks Block (2 columns)
                                    if (result.tracks.isNotEmpty()) {
                                        item {
                                            su.nnedition.kord.ui.screen.main.page.search.section.TracksSection(
                                                tracks = result.tracks,
                                                currentTrack = currentTrack,
                                                playerState = playerState,
                                                page = trackPage,
                                                onPageChange = { trackPage = it },
                                                onTrackClick = { track ->
                                                    player.setQueue(
                                                        tracks = result.tracks,
                                                        currentTrack = track,
                                                        autoPlay = true
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    // Albums Block
                                    if (result.albums.isNotEmpty()) {
                                        item {
                                            su.nnedition.kord.ui.screen.main.page.search.section.AlbumsSection(
                                                albums = result.albums,
                                                onAlbumClick = { album ->
                                                    //@TODO Future: navigate to album or play
                                                }
                                            )
                                        }
                                    }

                                    // Artists Block
                                    if (result.artists.isNotEmpty()) {
                                        item {
                                            su.nnedition.kord.ui.screen.main.page.search.section.ArtistsSection(
                                                artists = result.artists,
                                                onArtistClick = { artist ->
                                                    //@TODO Future: navigate to artist
                                                }
                                            )
                                        }
                                    }
                                }

                                su.nnedition.kord.ui.screen.main.page.search.SearchCategory.TRACKS -> {
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
                                                su.nnedition.kord.ui.screen.main.page.search.SearchTrackGridItem(
                                                    track = track,
                                                    isPlaying = isPlaying,
                                                    onClick = {
                                                        player.setQueue(
                                                            tracks = result.tracks,
                                                            currentTrack = track,
                                                            autoPlay = true
                                                        )
                                                    }
                                                )
                                            }

                                            if (pair.size > 1) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    val track = pair[1]
                                                    val isPlaying = currentTrack?.id == track.id && playerState == PlayerState.PLAYING
                                                    su.nnedition.kord.ui.screen.main.page.search.SearchTrackGridItem(
                                                        track = track,
                                                        isPlaying = isPlaying,
                                                        onClick = {
                                                            player.setQueue(
                                                                tracks = result.tracks,
                                                                currentTrack = track,
                                                                autoPlay = true
                                                            )
                                                        }
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }

                                su.nnedition.kord.ui.screen.main.page.search.SearchCategory.ALBUMS -> {
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
                                                su.nnedition.kord.ui.screen.main.page.search.SearchAlbumCard(
                                                    album = album,
                                                    onClick = {}
                                                )
                                            }
                                        }
                                    }
                                }

                                su.nnedition.kord.ui.screen.main.page.search.SearchCategory.ARTISTS -> {
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
                                                su.nnedition.kord.ui.screen.main.page.search.SearchArtistCard(
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

@Preview
@Composable
private fun SearchPagePreview() {
    su.nnedition.kord.ui.screen.main.page.search.SearchPage.Content(modifier = Modifier.fillMaxSize())
}
