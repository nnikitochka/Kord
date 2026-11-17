package su.nnedition.kord.ui.navigation

import androidx.navigation.NavController

actual object PlatformRouter {
    actual fun bindNavController(navController: NavController) {
        // Desktop uses in-memory navigation
    }
}
