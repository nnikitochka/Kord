package su.nnedition.kord.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppNavigator(
    initialScreen: AppScreen = ServerSelectRoute
) {
    private val backStack = mutableListOf<AppScreen>(initialScreen)
    private val _currentScreen = MutableStateFlow<AppScreen>(initialScreen)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val canPop: Boolean
        get() = backStack.size > 1

    fun navigateTo(screen: AppScreen) {
        backStack.add(screen)
        _currentScreen.value = screen
    }

    fun replace(screen: AppScreen) {
        if (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.lastIndex)
        }
        backStack.add(screen)
        _currentScreen.value = screen
    }

    fun pop(): Boolean {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
            _currentScreen.value = backStack.last()
            return true
        }
        return false
    }

    companion object {
        val default: AppNavigator by lazy { AppNavigator() }
    }
}
