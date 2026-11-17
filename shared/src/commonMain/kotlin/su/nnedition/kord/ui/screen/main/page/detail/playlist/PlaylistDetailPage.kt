package su.nnedition.kord.ui.screen.main.page.detail.playlist

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import su.nnedition.kord.data.model.Playlist

@Composable
fun PlaylistDetailPage(
    playlistId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    PlaylistDetailView(
        playlist = Playlist(id = playlistId, name = ""),
        onBack = onBack,
        modifier = modifier
    )
}
