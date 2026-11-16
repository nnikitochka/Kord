package su.nnedition.kord.ui.screen.main.page.detail

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import su.nnedition.kord.data.model.Album
import su.nnedition.kord.data.model.Artist
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.ui.component.image.CoverImage
import su.nnedition.kord.ui.screen.main.page.search.SearchAlbumCard

@Composable
fun ArtistDetailsPage(
    artistId: String,
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    val client = remember { KordClient.default }
    val coroutineScope = rememberCoroutineScope()

    var artist by remember(artistId) { mutableStateOf<Artist?>(null) }
    var isLoading by remember(artistId) { mutableStateOf(true) }
    var errorMessage by remember(artistId) { mutableStateOf<String?>(null) }

    fun loadArtist() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            client.getArtist(artistId)
                .onSuccess { detailed ->
                    artist = detailed
                    isLoading = false
                }
                .onFailure { ex ->
                    errorMessage = ex.message ?: "Не удалось загрузить данные исполнителя"
                    isLoading = false
                }
        }
    }

    fun toggleArtistFavorite() {
        val current = artist ?: return
        val newStarred = !current.isStarred
        artist = current.copy(isStarred = newStarred)
        coroutineScope.launch {
            val result = if (newStarred) client.star(artistIds = listOf(current.id)) else client.unstar(artistIds = listOf(current.id))
            if (result.isFailure) {
                artist = current
            }
        }
    }

    LaunchedEffect(artistId) {
        loadArtist()
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
                        Button(onClick = { loadArtist() }) {
                            Text("Повторить")
                        }
                    }
                }
            }

            artist != null -> {
                val currentArtist = artist!!
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Span
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            CoverImage(
                                url = currentArtist.coverArtUrl,
                                contentDescription = currentArtist.name,
                                modifier = Modifier.size(140.dp),
                                shape = CircleShape,
                                fallbackIcon = Icons.Default.Person,
                                iconSize = 64.dp
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ИСПОЛНИТЕЛЬ",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentArtist.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${currentArtist.albumCount} альбомов",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                IconButton(
                                    onClick = { toggleArtistFavorite() },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (currentArtist.isStarred) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (currentArtist.isStarred) "Удалить из избранного" else "В избранное",
                                        tint = if (currentArtist.isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Section Title Span
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Дискография",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    // Albums
                    items(
                        items = currentArtist.albums,
                        key = { it.id }
                    ) { album ->
                        SearchAlbumCard(
                            album = album,
                            onClick = { onAlbumClick(album) }
                        )
                    }
                }
            }
        }
    }
}
