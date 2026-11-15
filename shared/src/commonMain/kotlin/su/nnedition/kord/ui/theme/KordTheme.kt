package su.nnedition.kord.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
//    background = darkGray,
//    surface = mediumGray, // Card
//    surfaceContainerHighest = mediumGray, // Card
//    primary = lightGray,
//    onPrimary = white
)

val Int.scaledDp: Dp
    @Composable
    get() = (this * LocalDensity.current.density).dp

@Composable
fun KordAppTheme(
    uiScale: Int = 120,
    content: @Composable () -> Unit
) {
    val currentDensity = LocalDensity.current
    val scaleFactor = (uiScale.coerceIn(50, 300)) / 100f
    val scaledDensity = remember(currentDensity.density, currentDensity.fontScale, scaleFactor) {
        Density(
            density = currentDensity.density * scaleFactor,
            fontScale = currentDensity.fontScale
        )
    }

    // Прокидываем новую плотность вниз по дереву компонентов
    CompositionLocalProvider(
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            shapes = Shapes,
            typography = Typography,
            content = content
        )
    }
}