package su.nnedition.kord.data.remote

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import su.nnedition.kord.data.model.Album
import su.nnedition.kord.data.model.AlbumListType
import su.nnedition.kord.data.model.Artist
import su.nnedition.kord.data.model.Playlist
import su.nnedition.kord.data.model.SearchResult
import su.nnedition.kord.data.model.Track
import su.nnedition.kord.data.remote.dto.KordinatorResponseDto
import su.nnedition.kord.data.remote.dto.KordinatorRootResponseDto
import su.nnedition.kord.data.remote.mapper.KordinatorMapper
import su.nnedition.kord.util.KermitKtorLogger
import kotlin.time.Duration.Companion.milliseconds

class KordClient(
    private var config: ServerConfig = ServerConfig()
) {
    val logger = Logger.withTag("KordClient")

    val httpClient = HttpClient {
        install(Logging) {
            logger = KermitKtorLogger("KordHttpClient")
            level = LogLevel.ALL
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val mapper = KordinatorMapper { config }

    fun setSession(baseUrl: String, username: String, token: String) {
        config = ServerConfig(
            baseUrl = baseUrl.trim().trimEnd('/'),
            username = username,
            token = token
        )
    }

    fun clearSession() {
        config = ServerConfig()
    }

    fun updateConfig(newConfig: ServerConfig) {
        config = newConfig
    }

    fun getConfig(): ServerConfig = config

    fun getStreamUrl(trackId: String): String = mapper.getStreamUrl(trackId)

    fun getCoverArtUrl(coverArtId: String, size: Int? = null): String = mapper.getCoverArtUrl(coverArtId, size)

    suspend fun ping(baseUrl: String? = null): Boolean {
        val targetBase = (baseUrl ?: config.baseUrl).trim().trimEnd('/')
        if (targetBase.isEmpty()) return false

        val pingUrl = "$targetBase/rest/ping?v=1.15.0&c=kord&f=json"
        logger.d { "Пинг сервера: $pingUrl" }

        return withTimeoutOrNull(4000L.milliseconds) {
            try {
                val resp = httpClient.get(pingUrl)
                val ok = resp.status.value in 200..499
                logger.d { "Результат пинга $pingUrl: HTTP ${resp.status.value} ($ok)" }
                ok
            } catch (e: Exception) {
                logger.w { "Ошибка пинга $pingUrl: ${e.message}, пробуем корень $targetBase" }
                try {
                    val rootResp = httpClient.get(targetBase)
                    val ok = rootResp.status.value in 200..499
                    logger.d { "Результат пинга корня $targetBase: HTTP ${rootResp.status.value} ($ok)" }
                    ok
                } catch (e2: Exception) {
                    logger.w { "Корень $targetBase недоступен: ${e2.message}" }
                    false
                }
            }
        } ?: run {
            logger.w { "Таймаут пинга для $targetBase" }
            false
        }
    }

    suspend fun login(baseUrl: String, username: String, password: String): Result<String> = runCatching {
        val cleanBase = baseUrl.trim().trimEnd('/')
        val pingUrl = "$cleanBase/rest/ping?u=$username&p=$password&v=1.15.0&c=kord&f=json"
        val maskedUrl = "$cleanBase/rest/ping?u=$username&p=******&v=1.15.0&c=kord&f=json"
        logger.i { "Отправка запроса авторизации: $maskedUrl" }

        val response = httpClient.get(pingUrl)
        val body = response.bodyAsText().trim()
        logger.i { "Ответ сервера: HTTP ${response.status.value} ${response.status.description}" }
        logger.d { "Тело ответа: $body" }

        if (response.status.value in 200..299) {
            val isOk = body.contains("\"status\"\\s*:\\s*\"ok\"".toRegex(RegexOption.IGNORE_CASE))
            if (isOk) {
                logger.i { "Авторизация в Kordinator успешно подтверждена" }
                password
            } else {
                val errorMsg = extractErrorMessage(body) ?: "Неверный логин или пароль"
                error(errorMsg)
            }
        } else {
            error("Ошибка сервера (${response.status.value}): $body")
        }
    }.onFailure { error ->
        logger.e(error) { "Сбой при вызове login(): ${error.message}" }
    }

    private suspend fun executeKordinatorRequest(
        endpoint: String,
        queryParams: Map<String, String> = emptyMap()
    ): Result<KordinatorResponseDto> = runCatching {
        val cleanBase = config.baseUrl.trim().trimEnd('/')
        require(cleanBase.isNotEmpty()) { "URL сервера не настроен" }

        val username = config.username
        val password = config.token.orEmpty()
        val authParams = "u=$username&p=$password&v=1.15.0&c=kord&f=json"
        val additionalParams = if (queryParams.isNotEmpty()) {
            "&" + queryParams.entries.joinToString("&") { (k, v) ->
                "$k=${v.encodeURLParameter()}"
            }
        } else ""

        val fullUrl = "$cleanBase/rest/$endpoint?$authParams$additionalParams"
        val maskedUrl = "$cleanBase/rest/$endpoint?u=$username&p=******&v=1.15.0&c=kord&f=json$additionalParams"
        logger.d { "Запрос Kordinator API: $maskedUrl" }

        val response = httpClient.get(fullUrl)
        val body = response.bodyAsText().trim()

        if (response.status.value !in 200..299) {
            error("Ошибка сервера (${response.status.value}): $body")
        }

        val rootDto = json.decodeFromString<KordinatorRootResponseDto>(body)
        val subResponse = rootDto.response
        if (subResponse.status.equals("ok", ignoreCase = true)) {
            subResponse
        } else {
            val errorMsg = subResponse.error?.message
                ?: extractErrorMessage(body)
                ?: "Ошибка Kordinator API"
            error(errorMsg)
        }
    }

    suspend fun getArtists(): Result<List<Artist>> = runCatching {
        val response = executeKordinatorRequest("getArtists").getOrThrow()
        response.artists?.index?.flatMap { it.artist }?.map { mapper.toArtist(it) } ?: emptyList()
    }.onFailure { error ->
        logger.e(error) { "Ошибка при получении артистов: ${error.message}" }
    }

    suspend fun getArtist(id: String): Result<Artist> = runCatching {
        val response = executeKordinatorRequest("getArtist", mapOf("id" to id)).getOrThrow()
        response.artist?.let { mapper.toArtist(it) } ?: error("Артист с id=$id не найден")
    }.onFailure { error ->
        logger.e(error) { "Ошибка при получении артиста id=$id: ${error.message}" }
    }

    suspend fun getAlbumList(
        type: AlbumListType = AlbumListType.RECENT,
        size: Int = 20,
        offset: Int = 0
    ): Result<List<Album>> = runCatching {
        val params = mapOf(
            "type" to type.value,
            "size" to size.toString(),
            "offset" to offset.toString()
        )
        val response = executeKordinatorRequest("getAlbumList2", params).getOrThrow()
        response.albumList2?.album?.map { mapper.toAlbum(it) } ?: emptyList()
    }.onFailure { error ->
        logger.e(error) { "Ошибка при получении списка альбомов (тип ${type.value}): ${error.message}" }
    }

    suspend fun getAlbum(id: String): Result<Album> = runCatching {
        val response = executeKordinatorRequest("getAlbum", mapOf("id" to id)).getOrThrow()
        response.album?.let { mapper.toAlbum(it) } ?: error("Альбом с id=$id не найден")
    }.onFailure { error ->
        logger.e(error) { "Ошибка при получении альбома id=$id: ${error.message}" }
    }

    suspend fun getTrack(id: String): Result<Track> = runCatching {
        val response = executeKordinatorRequest("getSong", mapOf("id" to id)).getOrThrow()
        response.song?.let { mapper.toTrack(it) } ?: error("Трек с id=$id не найден")
    }.onFailure { error ->
        logger.e(error) { "Ошибка при получении трека id=$id: ${error.message}" }
    }

    suspend fun search(
        query: String,
        artistCount: Int = 20,
        albumCount: Int = 20,
        songCount: Int = 20
    ): Result<SearchResult> = runCatching {
        val params = mapOf(
            "query" to query,
            "artistCount" to artistCount.toString(),
            "albumCount" to albumCount.toString(),
            "songCount" to songCount.toString()
        )
        val response = executeKordinatorRequest("search3", params).getOrThrow()
        response.searchResult3?.let { mapper.toSearchResult(it) } ?: SearchResult()
    }.onFailure { error ->
        logger.e(error) { "Ошибка при поиске '$query': ${error.message}" }
    }

    suspend fun getPlaylists(): Result<List<Playlist>> = runCatching {
        val response = executeKordinatorRequest("getPlaylists").getOrThrow()
        response.playlists?.playlist?.map { mapper.toPlaylist(it) } ?: emptyList()
    }.onFailure { error ->
        logger.e(error) { "Ошибка при получении плейлистов: ${error.message}" }
    }

    suspend fun getPlaylist(id: String): Result<Playlist> = runCatching {
        val response = executeKordinatorRequest("getPlaylist", mapOf("id" to id)).getOrThrow()
        response.playlist?.let { mapper.toPlaylist(it) } ?: error("Плейлист с id=$id не найден")
    }.onFailure { error ->
        logger.e(error) { "Ошибка при получении плейлиста id=$id: ${error.message}" }
    }

    suspend fun createPlaylist(name: String): Result<Unit> = runCatching {
        executeKordinatorRequest("createPlaylist", mapOf("name" to name)).getOrThrow()
        Unit
    }.onFailure { error ->
        logger.e(error) { "Ошибка при создании плейлиста '$name': ${error.message}" }
    }

    private fun extractErrorMessage(body: String): String? {
        val match = """"message"\s*:\s*"([^"]+)"""".toRegex().find(body)
        val rawMessage = match?.groupValues?.getOrNull(1) ?: return null
        return when {
            rawMessage.contains("Wrong username or password", ignoreCase = true) -> "Неверный логин или пароль"
            rawMessage.contains("missing parameter", ignoreCase = true) -> "Отсутствует обязательный параметр"
            else -> rawMessage
        }
    }

    fun close() {
        httpClient.close()
    }

    companion object {
        val default: KordClient by lazy { KordClient() }
    }
}

