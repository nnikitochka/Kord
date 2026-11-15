package su.nnedition.kord.ui.component.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import su.nnedition.kord.data.model.Playlist
import su.nnedition.kord.data.remote.KordClient

object PlaylistsPage : Page {
    override val title: String = "Плейлисты"
    override val icon: ImageVector = Icons.AutoMirrored.Filled.PlaylistPlay

    private val playlistGradients = listOf(
        listOf(Color(0xFF6B46C1), Color(0xFF9F7AEA)), // Purple
        listOf(Color(0xFFDD6B20), Color(0xFFF6AD55)), // Orange
        listOf(Color(0xFFE53E3E), Color(0xFFFC8181)), // Red
        listOf(Color(0xFF319795), Color(0xFF4FD1C5)), // Teal
        listOf(Color(0xFF3182CE), Color(0xFF63B3ED)), // Blue
        listOf(Color(0xFFD53F8C), Color(0xFFF687B3)), // Pink
        listOf(Color(0xFF4A5568), Color(0xFF718096)), // Slate
    )

    @Composable
    override fun Content(modifier: Modifier) {
        val client = remember { KordClient.default }
        val coroutineScope = rememberCoroutineScope()

        var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        var selectedFilter by remember { mutableStateOf(PlaylistFilter.CREATED) }
        var showCreateDialog by remember { mutableStateOf(false) }
        var newPlaylistName by remember { mutableStateOf("") }
        var isCreating by remember { mutableStateOf(false) }

        fun loadPlaylists() {
            isLoading = true
            errorMessage = null
            coroutineScope.launch {
                client.getPlaylists()
                    .onSuccess { list ->
                        playlists = list
                        isLoading = false
                    }
                    .onFailure { ex ->
                        errorMessage = ex.message ?: "Не удалось загрузить плейлисты"
                        isLoading = false
                    }
            }
        }

        LaunchedEffect(Unit) {
            loadPlaylists()
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header: Title with optional back button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { /* TODO: back navigation if opened from subscreen */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Плейлисты",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-filters row (chips)
            val createdCount = playlists.count { !it.isPublic }
            val favoritesCount = playlists.count { it.isPublic }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedFilter == PlaylistFilter.CREATED,
                    onClick = { selectedFilter = PlaylistFilter.CREATED },
                    label = { Text("Вы собрали • $createdCount") },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                FilterChip(
                    selected = selectedFilter == PlaylistFilter.FAVORITES,
                    onClick = { selectedFilter = PlaylistFilter.FAVORITES },
                    label = { Text("Вам понравилось • $favoritesCount") },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main body
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage ?: "Произошла ошибка",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { loadPlaylists() }) {
                                Text("Повторить")
                            }
                        }
                    }
                }
                else -> {
                    val filteredPlaylists = when (selectedFilter) {
                        PlaylistFilter.CREATED -> playlists.filter { !it.isPublic }
                        PlaylistFilter.FAVORITES -> playlists.filter { it.isPublic }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // "Новый плейлист" card (first item in "Вы собрали")
                        if (selectedFilter == PlaylistFilter.CREATED) {
                            item {
                                NewPlaylistCard(onClick = { showCreateDialog = true })
                            }
                        }

                        // Playlists items
                        items(filteredPlaylists, key = { it.id }) { playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                onClick = { /* TODO: open playlist details */ }
                            )
                        }
                    }
                }
            }
        }

        // Create Playlist Dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isCreating) {
                        showCreateDialog = false
                        newPlaylistName = ""
                    }
                },
                title = { Text("Новый плейлист") },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Название") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreating
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = newPlaylistName.trim()
                            if (trimmed.isNotEmpty()) {
                                isCreating = true
                                coroutineScope.launch {
                                    client.createPlaylist(trimmed)
                                        .onSuccess {
                                            isCreating = false
                                            showCreateDialog = false
                                            newPlaylistName = ""
                                            loadPlaylists()
                                        }
                                        .onFailure {
                                            isCreating = false
                                        }
                                }
                            }
                        },
                        enabled = newPlaylistName.isNotBlank() && !isCreating
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Создать")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showCreateDialog = false
                            newPlaylistName = ""
                        },
                        enabled = !isCreating
                    ) {
                        Text("Отмена")
                    }
                }
            )
        }
    }

    @Composable
    private fun NewPlaylistCard(onClick: () -> Unit) {
        Column(
            modifier = Modifier
                .width(150.dp)
                .clickable(onClick = onClick)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Новый плейлист",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Новый плейлист",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun PlaylistCard(
        playlist: Playlist,
        onClick: () -> Unit
    ) {
        val gradient = remember(playlist.id) {
            val idx = (playlist.id.hashCode() and 0x7FFFFFFF) % playlistGradients.size
            playlistGradients[idx]
        }

        Column(
            modifier = Modifier
                .width(150.dp)
                .clickable(onClick = onClick)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(gradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.songCount} треков",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
