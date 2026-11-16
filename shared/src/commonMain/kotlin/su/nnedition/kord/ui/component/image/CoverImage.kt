package su.nnedition.kord.ui.component.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage

@Composable
fun CoverImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    accentColor: String? = null,
    fallbackIcon: ImageVector = Icons.Default.MusicNote,
    iconSize: Dp = 24.dp,
    contentScale: ContentScale = ContentScale.Crop
) {
    val gradient = remember(accentColor) {
        val baseAccent = parseHexColor(accentColor)
        if (baseAccent != null) {
            listOf(baseAccent, baseAccent.copy(alpha = 0.7f))
        } else {
            listOf(Color(0xFF3182CE), Color(0xFF63B3ED))
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = fallbackIcon,
                            contentDescription = contentDescription,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = fallbackIcon,
                            contentDescription = contentDescription,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            )
        } else {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

private fun parseHexColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        val clean = hex.removePrefix("#")
        when (clean.length) {
            6 -> Color(clean.toLong(16) or 0xFF000000L)
            8 -> Color(clean.toLong(16))
            else -> null
        }
    } catch (_: Throwable) {
        null
    }
}

@Preview
@Composable
private fun CoverImagePreview() {
    CoverImage(
        url = null,
        contentDescription = "Preview",
        modifier = Modifier.size(60.dp),
        accentColor = "#FF5722"
    )
}
