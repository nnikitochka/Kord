package su.nnedition.kord.ui.screen.common

import androidx.compose.runtime.Composable

@Composable
fun PageNavButton(
    page: Page,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavIconButton(
        icon = page.icon,
        contentDescription = page.title,
        isSelected = isSelected,
        onClick = onClick
    )
}
