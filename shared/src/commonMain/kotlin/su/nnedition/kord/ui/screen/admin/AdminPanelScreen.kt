package su.nnedition.kord.ui.screen.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import su.nnedition.kord.ui.navigation.AppNavigator
import su.nnedition.kord.ui.screen.admin.page.AdminFoldersPage
import su.nnedition.kord.ui.screen.admin.page.AdminServerPage
import su.nnedition.kord.ui.screen.admin.page.AdminUsersPage
import su.nnedition.kord.ui.screen.common.CommonViewScreen
import su.nnedition.kord.ui.screen.common.NavIconButton
import su.nnedition.kord.ui.screen.common.Page

@Composable
@Preview
fun AdminPanelScreen(
    topPages: List<Page> = listOf(AdminUsersPage, AdminFoldersPage, AdminServerPage),
    bottomPages: List<Page> = emptyList(),
    onBack: () -> Unit = { AppNavigator.default.pop() }
) {
    CommonViewScreen(
        topPages = topPages,
        bottomPages = bottomPages,
        afterBottomPages = {
            NavIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад в плеер",
                isSelected = false,
                onClick = onBack
            )
        }
    )
}
