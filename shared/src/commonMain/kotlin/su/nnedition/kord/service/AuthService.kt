package su.nnedition.kord.service

import su.nnedition.kord.data.model.UserSession
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.data.repository.AuthRepository

class AuthService(
    private val remoteClient: KordClient = KordClient.default,
    private val authRepository: AuthRepository = AuthRepository.default
) {
    suspend fun ping(baseUrl: String): Boolean {
        return remoteClient.ping(baseUrl)
    }

    suspend fun login(baseUrl: String, username: String, password: String): Result<UserSession> {
        val cleanUrl = baseUrl.trim().trimEnd('/')
        return remoteClient.login(cleanUrl, username, password).map { token ->
            val session = UserSession(
                serverUrl = cleanUrl,
                username = username,
                token = token
            )
            authRepository.saveSession(session)
            remoteClient.setSession(session.serverUrl, session.username, session.token)
            session
        }
    }

    fun restoreSession(): UserSession? {
        val session = authRepository.getSession() ?: return null
        remoteClient.setSession(session.serverUrl, session.username, session.token)
        return session
    }

    fun logout() {
        authRepository.clear()
        remoteClient.clearSession()
    }

    companion object {
        val default: AuthService by lazy { AuthService() }
    }
}
