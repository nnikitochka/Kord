package su.nnedition.kord.util

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.logging.Logger as KtorLogger

class KermitKtorLogger(
    private val tag: String = "KordHttp"
) : KtorLogger {
    override fun log(message: String) {
        Logger.withTag(tag).i { message }
    }
}
