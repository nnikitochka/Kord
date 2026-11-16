package su.nnedition.kord.ui.component.page.search.section

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import su.nnedition.kord.data.model.Artist
import su.nnedition.kord.ui.component.page.search.SearchArtistCard

@Composable
fun ArtistsSection(
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