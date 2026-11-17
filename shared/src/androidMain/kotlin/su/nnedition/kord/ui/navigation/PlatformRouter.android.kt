package su.nnedition.kord.ui.navigation

import androidx.navigation.NavController

actual object PlatformRouter {
    actual fun bindNavController(navController: NavController) {
        // Android handles system back / deep links natively
    }
}
