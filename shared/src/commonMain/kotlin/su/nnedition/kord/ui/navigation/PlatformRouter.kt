package su.nnedition.kord.ui.navigation

import androidx.navigation.NavController

expect object PlatformRouter {
    fun bindNavController(navController: NavController)
}
