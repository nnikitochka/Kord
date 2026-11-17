package su.nnedition.kord.ui.screen.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import su.nnedition.kord.service.AuthService
import su.nnedition.kord.ui.navigation.AdminPanelRoute
import su.nnedition.kord.ui.navigation.AppNavigator
import su.nnedition.kord.ui.screen.common.CommonViewScreen
import su.nnedition.kord.ui.screen.common.NavIconButton
import su.nnedition.kord.ui.screen.common.Page
import su.nnedition.kord.ui.screen.common.page.SettingsPage
import su.nnedition.kord.ui.screen.main.page.CollectionsPage
import su.nnedition.kord.ui.screen.main.page.HomePage
import su.nnedition.kord.ui.screen.main.page.playlist.PlaylistsPage
import su.nnedition.kord.ui.screen.main.page.search.SearchPage

@Composable
@Preview
fun MainViewScreen(
    topPages: List<Page> = listOf(HomePage, SearchPage, PlaylistsPage, CollectionsPage),
    bottomPages: List<Page> = listOf(SettingsPage),
    onNavigateToAdmin: () -> Unit = { AppNavigator.default.navigateTo(AdminPanelRoute) }
) {
    val currentSession by AuthService.default.currentSession.collectAsState()
    val isAdmin = currentSession?.isAdmin == true

    CommonViewScreen(
        topPages = topPages,
        bottomPages = bottomPages,
        beforeBottomPages = {
            if (isAdmin) {
                NavIconButton(
                    icon = Icons.Default.AdminPanelSettings,
                    contentDescription = "Панель администратора",
                    isSelected = false,
                    onClick = onNavigateToAdmin
                )
            }
        }
    )
}
