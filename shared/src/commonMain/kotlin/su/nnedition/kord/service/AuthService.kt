package su.nnedition.kord.service

import co.touchlab.kermit.Logger
import su.nnedition.kord.data.model.UserSession
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.data.repository.AuthRepository

class AuthService(
    private val remoteClient: KordClient = KordClient.default,
    private val authRepository: AuthRepository = AuthRepository.default
) {
    private val logger = Logger.withTag("AuthService")

    suspend fun ping(baseUrl: String): Boolean {
        return remoteClient.ping(baseUrl)
    }

    suspend fun login(baseUrl: String, username: String, password: String): Result<UserSession> {
        val cleanUrl = baseUrl.trim().trimEnd('/')
        logger.i { "Запуск сценария логина для '$username' на $cleanUrl" }
        return remoteClient.login(cleanUrl, username, password).map { token ->
            val session = UserSession(
                serverUrl = cleanUrl,
                username = username,
                token = token
            )
            logger.i { "Сохранение сессии в AuthRepository..." }
            authRepository.saveSession(session)
            remoteClient.setSession(session.serverUrl, session.username, session.token)
            logger.i { "Сессия успешно установлена для '${session.username}'" }
            session
        }.onFailure { error ->
            logger.e { "Логин завершился ошибкой: ${error.message}" }
        }
    }

    fun restoreSession(): UserSession? {
        val session = authRepository.getSession()
        if (session != null) {
            logger.i { "Восстановлена сохранённая сессия для '${session.username}' (${session.serverUrl})" }
            remoteClient.setSession(session.serverUrl, session.username, session.token)
        } else {
            logger.d { "Сохранённая сессия отсутствует в репозитории" }
        }
        return session
    }

    fun logout() {
        logger.i { "Выход из аккаунта (очистка сессии)" }
        authRepository.clear()
        remoteClient.clearSession()
    }

    companion object {
        val default: AuthService by lazy { AuthService() }
    }
}
