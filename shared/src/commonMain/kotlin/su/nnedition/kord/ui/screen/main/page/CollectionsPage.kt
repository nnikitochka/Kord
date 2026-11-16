package su.nnedition.kord.ui.screen.main.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import su.nnedition.kord.ui.screen.common.Page

object CollectionsPage : Page {
    override val title: String = "Коллекции"
    override val icon: ImageVector = Icons.Default.LibraryMusic

    @Composable
    override fun Content(modifier: Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text("Коллекции")
        }
    }
}

@Preview
@Composable
private fun CollectionsPagePreview() {
    CollectionsPage.Content(modifier = Modifier.fillMaxSize())
}

