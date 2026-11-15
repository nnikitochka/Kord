package su.nnedition.kord.data.repository

import su.nnedition.kord.data.model.UserSession

interface AuthRepository {
    fun saveSession(session: UserSession)
    fun getSession(): UserSession?
    fun clear()

    companion object {
        val default: AuthRepository by lazy { SettingsAuthRepository() }
    }
}
