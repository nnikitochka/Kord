package su.nnedition.kord.ui.screen.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import su.nnedition.kord.player.PlayerManager
import su.nnedition.kord.service.AuthService
import su.nnedition.kord.ui.component.MiniPlayer
import su.nnedition.kord.ui.navigation.AdminFoldersRoute
import su.nnedition.kord.ui.navigation.AdminServerRoute
import su.nnedition.kord.ui.navigation.AdminUsersRoute
import su.nnedition.kord.ui.navigation.AlbumDetailRoute
import su.nnedition.kord.ui.navigation.ArtistDetailRoute
import su.nnedition.kord.ui.navigation.CollectionsRoute
import su.nnedition.kord.ui.navigation.HomeRoute
import su.nnedition.kord.ui.navigation.LocalNavController
import su.nnedition.kord.ui.navigation.PlatformRouter
import su.nnedition.kord.ui.navigation.PlaylistDetailRoute
import su.nnedition.kord.ui.navigation.PlaylistsRoute
import su.nnedition.kord.ui.navigation.SearchRoute
import su.nnedition.kord.ui.navigation.SettingsRoute
import su.nnedition.kord.ui.screen.admin.page.AdminFoldersPage
import su.nnedition.kord.ui.screen.admin.page.AdminServerPage
import su.nnedition.kord.ui.screen.admin.page.AdminUsersPage
import su.nnedition.kord.ui.screen.common.NavIconButton
import su.nnedition.kord.ui.screen.common.page.SettingsPage
import su.nnedition.kord.ui.screen.main.page.CollectionsPage
import su.nnedition.kord.ui.screen.main.page.HomePage
import su.nnedition.kord.ui.screen.main.page.detail.AlbumDetailsPage
import su.nnedition.kord.ui.screen.main.page.detail.ArtistDetailsPage
import su.nnedition.kord.ui.screen.main.page.detail.playlist.PlaylistDetailPage
import su.nnedition.kord.ui.screen.main.page.playlist.PlaylistsPage
import su.nnedition.kord.ui.screen.main.page.search.SearchPage

@Composable
fun AppShellScreen(
    navController: NavHostController = rememberNavController()
) {
    LaunchedEffect(navController) {
        PlatformRouter.bindNavController(navController)
        if (PlayerManager.instance.currentTrack.value == null) {
            PlayerManager.restoreQueue()
        }
    }

    val currentSession by AuthService.default.currentSession.collectAsState()
    val isAdmin = currentSession?.isAdmin == true

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val isInAdmin = currentDestination?.hasRoute<AdminUsersRoute>() == true ||
            currentDestination?.hasRoute<AdminFoldersRoute>() == true ||
            currentDestination?.hasRoute<AdminServerRoute>() == true

    CompositionLocalProvider(LocalNavController provides navController) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Sidebar
            Card(
                modifier = Modifier.fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isInAdmin) {
                        NavIconButton(
                            icon = Icons.Default.People,
                            contentDescription = "Пользователи",
                            isSelected = currentDestination.hasRoute<AdminUsersRoute>(),
                            onClick = {
                                navController.navigate(AdminUsersRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavIconButton(
                            icon = Icons.Default.Folder,
                            contentDescription = "Папки",
                            isSelected = currentDestination.hasRoute<AdminFoldersRoute>(),
                            onClick = {
                                navController.navigate(AdminFoldersRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavIconButton(
                            icon = Icons.Default.Dns,
                            contentDescription = "Сервер",
                            isSelected = currentDestination.hasRoute<AdminServerRoute>(),
                            onClick = {
                                navController.navigate(AdminServerRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        NavIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад в плеер",
                            isSelected = false,
                            onClick = {
                                navController.navigate(HomeRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    } else {
                        NavIconButton(
                            icon = Icons.Default.Home,
                            contentDescription = "Главная",
                            isSelected = currentDestination?.hasRoute<HomeRoute>() == true,
                            onClick = {
                                navController.navigate(HomeRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavIconButton(
                            icon = Icons.Default.Search,
                            contentDescription = "Поиск",
                            isSelected = currentDestination?.hasRoute<SearchRoute>() == true,
                            onClick = {
                                navController.navigate(SearchRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavIconButton(
                            icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                            contentDescription = "Плейлисты",
                            isSelected = currentDestination?.hasRoute<PlaylistsRoute>() == true || currentDestination?.hasRoute<PlaylistDetailRoute>() == true,
                            onClick = {
                                navController.navigate(PlaylistsRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavIconButton(
                            icon = Icons.Default.LibraryMusic,
                            contentDescription = "Коллекции",
                            isSelected = currentDestination?.hasRoute<CollectionsRoute>() == true,
                            onClick = {
                                navController.navigate(CollectionsRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        if (isAdmin) {
                            NavIconButton(
                                icon = Icons.Default.AdminPanelSettings,
                                contentDescription = "Панель администратора",
                                isSelected = false,
                                onClick = {
                                    navController.navigate(AdminUsersRoute) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        NavIconButton(
                            icon = Icons.Default.Settings,
                            contentDescription = "Настройки",
                            isSelected = currentDestination?.hasRoute<SettingsRoute>() == true,
                            onClick = {
                                navController.navigate(SettingsRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }

            // Main Content Area & Docked Player
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable<HomeRoute> {
                            HomePage.Content(modifier = Modifier.fillMaxSize())
                        }
                        composable<SearchRoute> {
                            SearchPage.Content(modifier = Modifier.fillMaxSize())
                        }
                        composable<PlaylistsRoute> {
                            PlaylistsPage.Content(modifier = Modifier.fillMaxSize())
                        }
                        composable<PlaylistDetailRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<PlaylistDetailRoute>()
                            PlaylistDetailPage(
                                playlistId = route.playlistId,
                                onBack = { navController.popBackStack() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable<CollectionsRoute> {
                            CollectionsPage.Content(modifier = Modifier.fillMaxSize())
                        }
                        composable<AlbumDetailRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<AlbumDetailRoute>()
                            AlbumDetailsPage(
                                albumId = route.albumId,
                                onBack = { navController.popBackStack() },
                                onArtistClick = { artistId ->
                                    navController.navigate(ArtistDetailRoute(artistId))
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable<ArtistDetailRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<ArtistDetailRoute>()
                            ArtistDetailsPage(
                                artistId = route.artistId,
                                onBack = { navController.popBackStack() },
                                onAlbumClick = { album ->
                                    navController.navigate(AlbumDetailRoute(album.id))
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable<SettingsRoute> {
                            SettingsPage.Content(modifier = Modifier.fillMaxSize())
                        }
                        composable<AdminUsersRoute> {
                            AdminUsersPage.Content(modifier = Modifier.fillMaxSize())
                        }
                        composable<AdminFoldersRoute> {
                            AdminFoldersPage.Content(modifier = Modifier.fillMaxSize())
                        }
                        composable<AdminServerRoute> {
                            AdminServerPage.Content(modifier = Modifier.fillMaxSize())
                        }
                    }
                }

                MiniPlayer(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
