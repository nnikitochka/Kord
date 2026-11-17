package su.nnedition.kord.service

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import su.nnedition.kord.data.model.UserSession
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.data.repository.AuthRepository

class AuthService(
    private val remoteClient: KordClient = KordClient.default,
    private val authRepository: AuthRepository = AuthRepository.default
) {
    private val logger = Logger.withTag("AuthService")

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    suspend fun ping(baseUrl: String): Boolean {
        return remoteClient.ping(baseUrl)
    }

    suspend fun login(baseUrl: String, username: String, password: String): Result<UserSession> {
        val cleanUrl = baseUrl.trim().trimEnd('/')
        logger.i { "Запуск сценария логина для '$username' на $cleanUrl" }
        return remoteClient.login(cleanUrl, username, password).map { token ->
            remoteClient.setSession(cleanUrl, username, token)

            // Проверяем роль администратора
            val userResult = remoteClient.getUser(username)
            val isAdmin = userResult.getOrNull()?.isAdmin == true
            logger.i { "Права пользователя '$username': isAdmin = $isAdmin" }

            val session = UserSession(
                serverUrl = cleanUrl,
                username = username,
                token = token,
                isAdmin = isAdmin
            )
            logger.i { "Сохранение сессии в AuthRepository..." }
            authRepository.saveSession(session)
            _currentSession.value = session
            logger.i { "Сессия успешно установлена для '${session.username}'" }
            session
        }.onFailure { error ->
            logger.e { "Логин завершился ошибкой: ${error.message}" }
        }
    }

    fun restoreSession(): UserSession? {
        val session = authRepository.getSession()
        if (session != null) {
            logger.i { "Восстановлена сохранённая сессия для '${session.username}' (isAdmin=${session.isAdmin})" }
            remoteClient.setSession(session.serverUrl, session.username, session.token)
            _currentSession.value = session
            return session
        } else {
            logger.d { "Сохранённая сессия отсутствует в репозитории" }
            _currentSession.value = null
            return null
        }
    }

    suspend fun refreshUserRole() {
        val session = _currentSession.value ?: return
        remoteClient.getUser(session.username).onSuccess { user ->
            val effectiveAdmin = user.isAdmin
            if (effectiveAdmin != session.isAdmin) {
                val updated = session.copy(isAdmin = effectiveAdmin)
                authRepository.saveSession(updated)
                _currentSession.value = updated
                logger.i { "Роль администратора обновлена: isAdmin=$effectiveAdmin" }
            }
        }
    }

    fun logout() {
        logger.i { "Выход из аккаунта (очистка сессии)" }
        authRepository.clear()
        remoteClient.clearSession()
        _currentSession.value = null
    }

    companion object {
        val default: AuthService by lazy { AuthService() }
    }
}
