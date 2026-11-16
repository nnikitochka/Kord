package su.nnedition.kord.ui.screen.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

interface Page {
    val title: String
    val icon: ImageVector

    @Composable
    fun Content(modifier: Modifier = Modifier.Companion)
}