package su.nnedition.kord.player

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import su.nnedition.kord.data.remote.KordClient

object PlayerManager {
    val instance: AudioPlayer by lazy { createAudioPlayer() }

    private val logger = Logger.withTag("PlayerManager")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val isRestoring = MutableStateFlow(false)

    init {
        // Auto-save queue on pause
        scope.launch {
            instance.state.collect { state ->
                if (!isRestoring.value && state == PlayerState.PAUSED && instance.positionMs.value > 0L) {
                    saveQueue()
                }
            }
        }

        // Auto-save queue when current track changes
        scope.launch {
            instance.currentTrack
                .distinctUntilChangedBy { it?.id }
                .collect { track ->
                    if (!isRestoring.value && track != null) {
                        saveQueue()
                    }
                }
        }

        // Periodic sync every 15 seconds during active playback
        scope.launch {
            while (isActive) {
                delay(15_000L)
                if (!isRestoring.value && instance.state.value == PlayerState.PLAYING) {
                    saveQueue()
                }
            }
        }
    }

    fun restoreQueue(client: KordClient = KordClient.default) {
        scope.launch {
            logger.i { "Запрос последней очереди воспроизведения с сервера..." }
            isRestoring.value = true
            try {
                val result = client.getPlayQueue()
                result.onSuccess { playQueue ->
                    if (playQueue.tracks.isNotEmpty()) {
                        val currentTrack = playQueue.tracks.firstOrNull { it.id == playQueue.currentTrackId }
                            ?: playQueue.tracks.firstOrNull()
                        if (currentTrack != null) {
                            logger.i { "Восстановлен последний трек: '${currentTrack.title}', позиция: ${playQueue.positionMs} мс" }
                            instance.setQueue(
                                tracks = playQueue.tracks,
                                currentTrack = currentTrack,
                                positionMs = playQueue.positionMs,
                                autoPlay = false
                            )
                        }
                    } else {
                        logger.i { "Очередь воспроизведения на сервере пуста" }
                    }
                }.onFailure { error ->
                    logger.w { "Не удалось получить очередь воспроизведения с сервера: ${error.message}" }
                }
            } finally {
                delay(500L)
                isRestoring.value = false
            }
        }
    }

    fun saveQueue(client: KordClient = KordClient.default) {
        val current = instance.currentTrack.value ?: return
        val queue = instance.queue.value
        val positionMs = instance.positionMs.value
        val ids = if (queue.isNotEmpty()) queue.map { it.id } else listOf(current.id)

        scope.launch {
            client.savePlayQueue(
                trackIds = ids,
                currentTrackId = current.id,
                positionMs = positionMs
            )
        }
    }
}
