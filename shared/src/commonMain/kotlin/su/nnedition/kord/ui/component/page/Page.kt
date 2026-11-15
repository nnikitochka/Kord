package su.nnedition.kord.ui.component.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

interface Page {
    val title: String
    val icon: ImageVector

    @Composable
    fun Content(modifier: Modifier = Modifier)

    @Composable
    fun open(modifier: Modifier = Modifier) = Content(modifier)
}
