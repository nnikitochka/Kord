package su.nnedition.kord.ui.screen.main.page.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SearchCategoryChips(
    selectedCategory: su.nnedition.kord.ui.screen.main.page.search.SearchCategory,
    onCategorySelected: (su.nnedition.kord.ui.screen.main.page.search.SearchCategory) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<su.nnedition.kord.ui.screen.main.page.search.SearchCategory> = su.nnedition.kord.ui.screen.main.page.search.SearchCategory.entries
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()

            val backgroundColor by animateColorAsState(
                targetValue = when {
                    isSelected -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                },
                animationSpec = tween(durationMillis = 150)
            )

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                },
                animationSpec = tween(durationMillis = 150)
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else if (isHovered) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(durationMillis = 150)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(backgroundColor)
                    .border(
                        width = 1.5.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onCategorySelected(category) }
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor
                )
            }
        }
    }
}

@Preview
@Composable
private fun SearchCategoryChipsPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        su.nnedition.kord.ui.screen.main.page.search.SearchCategoryChips(
            selectedCategory = su.nnedition.kord.ui.screen.main.page.search.SearchCategory.TOP,
            onCategorySelected = {}
        )
    }
}
