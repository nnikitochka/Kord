package su.nnedition.kord.ui.component.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

object SettingsPage : Page {
    override val title: String = "Настройки"
    override val icon: ImageVector = Icons.Default.Settings

    @Composable
    override fun Content(modifier: Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text("Настройки")
        }
    }
}

@Preview
@Composable
private fun SettingsPagePreview() {
    SettingsPage.Content(modifier = Modifier.fillMaxSize())
}

