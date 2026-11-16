package su.nnedition.kord.ui.screen.main.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import su.nnedition.kord.ui.screen.common.Page

object HomePage : Page {
    override val title: String = "Главная"
    override val icon: ImageVector = Icons.Default.Home

    @Composable
    override fun Content(modifier: Modifier) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Главная")
        }
    }
}

@Preview
@Composable
private fun HomePagePreview() {
    su.nnedition.kord.ui.screen.main.page.HomePage.Content(modifier = Modifier.fillMaxSize())
}
