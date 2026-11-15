package su.nnedition.kord.ui.screen.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import su.nnedition.kord.ui.component.page.CollectionsPage
import su.nnedition.kord.ui.component.page.HomePage
import su.nnedition.kord.ui.component.page.Page
import su.nnedition.kord.ui.component.page.SearchPage
import su.nnedition.kord.ui.component.page.SettingsPage

@Composable
@Preview
fun MainViewScreen(
    topPages: List<Page> = listOf(HomePage, SearchPage, CollectionsPage),
    bottomPages: List<Page> = listOf(SettingsPage)
) {
    var selectedPage by remember { mutableStateOf(topPages.firstOrNull() ?: HomePage) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .padding(6.dp, 6.dp, 0.dp, 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                topPages.forEach { page ->
                    PageNavButton(
                        page = page,
                        isSelected = selectedPage == page,
                        onClick = { selectedPage = page }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                bottomPages.forEach { page ->
                    PageNavButton(
                        page = page,
                        isSelected = selectedPage == page,
                        onClick = { selectedPage = page }
                    )
                }
            }
        }

        Spacer(Modifier.padding(horizontal = 3.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(0.dp, 6.dp, 6.dp, 6.dp)
        ) {
            Crossfade(targetState = selectedPage) { page ->
                page.Content(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun PageNavButton(
    page: Page,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    IconButton(onClick = onClick) {
        Icon(
            imageVector = page.icon,
            contentDescription = page.title,
            tint = tint
        )
    }
}
