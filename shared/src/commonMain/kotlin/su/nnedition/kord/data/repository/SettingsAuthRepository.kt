package su.nnedition.kord.data.repository

import com.russhwolf.settings.Settings
import su.nnedition.kord.data.model.UserSession

class SettingsAuthRepository(
    private val settings: Settings = Settings()
) : AuthRepository {

    override fun saveSession(session: UserSession) {
        settings.putString(KEY_SERVER_URL, session.serverUrl)
        settings.putString(KEY_USERNAME, session.username)
        settings.putString(KEY_TOKEN, session.token)
    }

    override fun getSession(): UserSession? {
        val url = settings.getStringOrNull(KEY_SERVER_URL) ?: return null
        val user = settings.getStringOrNull(KEY_USERNAME) ?: return null
        val token = settings.getStringOrNull(KEY_TOKEN) ?: return null
        return UserSession(
            serverUrl = url,
            username = user,
            token = token
        )
    }

    override fun clear() {
        settings.remove(KEY_SERVER_URL)
        settings.remove(KEY_USERNAME)
        settings.remove(KEY_TOKEN)
    }

    companion object {
        private const val KEY_SERVER_URL = "auth_server_url"
        private const val KEY_USERNAME = "auth_username"
        private const val KEY_TOKEN = "auth_token"
    }
}
