@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package su.nnedition.kord.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.savedstate.SavedState
import androidx.savedstate.read
import kotlinx.browser.window

actual object PlatformRouter {
    private var isUpdatingFromUrl = false
    private var isUpdatingFromNav = false

    actual fun bindNavController(navController: NavController) {
        fun updateUrlForDestination(destination: androidx.navigation.NavDestination?, arguments: SavedState? = null) {
            if (isUpdatingFromUrl || destination == null) return
            isUpdatingFromNav = true
            try {
                val targetPath = when {
                    destination.hasRoute<HomeRoute>() -> "/"
                    destination.hasRoute<SearchRoute>() -> "/search"
                    destination.hasRoute<PlaylistsRoute>() -> "/playlists"
                    destination.hasRoute<PlaylistDetailRoute>() -> {
                        val id = arguments?.read { getStringOrNull("playlistId") } ?: ""
                        if (id.isNotEmpty()) "/playlist?id=$id" else "/playlists"
                    }
                    destination.hasRoute<CollectionsRoute>() -> "/collections"
                    destination.hasRoute<AlbumDetailRoute>() -> {
                        val id = arguments?.read { getStringOrNull("albumId") } ?: ""
                        if (id.isNotEmpty()) "/album?id=$id" else "/collections"
                    }
                    destination.hasRoute<ArtistDetailRoute>() -> {
                        val id = arguments?.read { getStringOrNull("artistId") } ?: ""
                        if (id.isNotEmpty()) "/artist?id=$id" else "/collections"
                    }
                    destination.hasRoute<SettingsRoute>() -> "/settings"
                    destination.hasRoute<AdminUsersRoute>() -> "/admin/users"
                    destination.hasRoute<AdminFoldersRoute>() -> "/admin/folders"
                    destination.hasRoute<AdminServerRoute>() -> "/admin/server"
                    else -> null
                }

                val currentFullUrl = window.location.pathname + window.location.search
                if (targetPath != null && currentFullUrl != targetPath) {
                    window.history.pushState(null, "", targetPath)
                }
            } finally {
                isUpdatingFromNav = false
            }
        }

        fun handleUrl(pathname: String, search: String) {
            if (isUpdatingFromNav) return
            val cleanPath = pathname.trim().removeSuffix("/")
            isUpdatingFromUrl = true
            try {
                when {
                    cleanPath.isEmpty() || cleanPath == "/home" -> {
                        if (navController.currentDestination?.hasRoute<HomeRoute>() != true) {
                            navController.navigate(HomeRoute) { launchSingleTop = true }
                        }
                    }
                    cleanPath == "/search" -> {
                        if (navController.currentDestination?.hasRoute<SearchRoute>() != true) {
                            navController.navigate(SearchRoute) { launchSingleTop = true }
                        }
                    }
                    cleanPath == "/playlists" -> {
                        if (navController.currentDestination?.hasRoute<PlaylistsRoute>() != true) {
                            navController.navigate(PlaylistsRoute) { launchSingleTop = true }
                        }
                    }
                    cleanPath.startsWith("/playlist") -> {
                        val id = search.substringAfter("id=", "").substringBefore("&")
                        if (id.isNotEmpty()) {
                            navController.navigate(PlaylistDetailRoute(id)) { launchSingleTop = true }
                        }
                    }
                    cleanPath == "/collections" -> {
                        if (navController.currentDestination?.hasRoute<CollectionsRoute>() != true) {
                            navController.navigate(CollectionsRoute) { launchSingleTop = true }
                        }
                    }
                    cleanPath.startsWith("/album") -> {
                        val id = search.substringAfter("id=", "").substringBefore("&")
                        if (id.isNotEmpty()) {
                            navController.navigate(AlbumDetailRoute(id)) { launchSingleTop = true }
                        }
                    }
                    cleanPath.startsWith("/artist") -> {
                        val id = search.substringAfter("id=", "").substringBefore("&")
                        if (id.isNotEmpty()) {
                            navController.navigate(ArtistDetailRoute(id)) { launchSingleTop = true }
                        }
                    }
                    cleanPath == "/settings" -> {
                        if (navController.currentDestination?.hasRoute<SettingsRoute>() != true) {
                            navController.navigate(SettingsRoute) { launchSingleTop = true }
                        }
                    }
                    cleanPath == "/admin/users" -> {
                        if (navController.currentDestination?.hasRoute<AdminUsersRoute>() != true) {
                            navController.navigate(AdminUsersRoute) { launchSingleTop = true }
                        }
                    }
                    cleanPath == "/admin/folders" -> {
                        if (navController.currentDestination?.hasRoute<AdminFoldersRoute>() != true) {
                            navController.navigate(AdminFoldersRoute) { launchSingleTop = true }
                        }
                    }
                    cleanPath == "/admin/server" -> {
                        if (navController.currentDestination?.hasRoute<AdminServerRoute>() != true) {
                            navController.navigate(AdminServerRoute) { launchSingleTop = true }
                        }
                    }
                }
            } finally {
                isUpdatingFromUrl = false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            updateUrlForDestination(destination, arguments)
        }

        window.onpopstate = {
            handleUrl(window.location.pathname, window.location.search)
        }

        // Support hash migration if someone opens a legacy hash URL
        val initialHash = window.location.hash.removePrefix("#")
        if (initialHash.isNotEmpty() && initialHash != "/") {
            val hashPath = initialHash.substringBefore("?")
            val hashSearch = if (initialHash.contains("?")) "?" + initialHash.substringAfter("?") else ""
            handleUrl(hashPath, hashSearch)
            window.history.replaceState(null, "", hashPath + hashSearch)
        } else if (window.location.pathname.isNotEmpty() && window.location.pathname != "/") {
            handleUrl(window.location.pathname, window.location.search)
        }
    }
}
