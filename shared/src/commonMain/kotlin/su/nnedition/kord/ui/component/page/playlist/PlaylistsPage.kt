package su.nnedition.kord.ui.component.page.playlist

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import su.nnedition.kord.data.model.Playlist
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.ui.component.page.Page
import su.nnedition.kord.ui.component.page.playlist.detail.PlaylistDetailView

object PlaylistsPage : Page {
    override val title: String = "Плейлисты"
    override val icon: ImageVector = Icons.AutoMirrored.Filled.PlaylistPlay

    val playlistGradients = listOf(
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

        var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
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
            // Header: Title with back button when inside playlist detail
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedPlaylist != null) {
                    IconButton(
                        onClick = { selectedPlaylist = null },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад к плейлистам",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Плейлисты",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Crossfade(
                targetState = selectedPlaylist,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { targetPlaylist ->
                if (targetPlaylist != null) {
                    PlaylistDetailView(
                        playlist = targetPlaylist,
                        onBack = { selectedPlaylist = null }
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
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
                                            onClick = { selectedPlaylist = playlist }
                                        )
                                    }
                                }
                            }
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
}

@Preview
@Composable
private fun PlaylistsPagePreview() {
    PlaylistsPage.Content(modifier = Modifier.fillMaxSize())
}
