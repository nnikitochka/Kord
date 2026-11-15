package su.nnedition.kord.player

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import su.nnedition.kord.data.model.Track
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.base.State
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent

class DesktopAudioPlayer : AudioPlayer {
    private val logger = Logger.withTag("DesktopAudioPlayer")
    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _currentTrack = MutableStateFlow<Track?>(null)
    override val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    override val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _state = MutableStateFlow(PlayerState.IDLE)
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    override val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    override val volume: StateFlow<Float> = _volume.asStateFlow()

    private var loadedMediaTrackId: String? = null

    private val playerComponent: AudioPlayerComponent? by lazy {
        try {
            val discovered = NativeDiscovery().discover()
            logger.i { "VLC NativeDiscovery результат: $discovered" }
            val component = AudioPlayerComponent()
            val mp = component.mediaPlayer()

            mp.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
                override fun playing(mediaPlayer: MediaPlayer?) {
                    _state.value = PlayerState.PLAYING
                }

                override fun paused(mediaPlayer: MediaPlayer?) {
                    _state.value = PlayerState.PAUSED
                }

                override fun stopped(mediaPlayer: MediaPlayer?) {
                    _state.value = PlayerState.IDLE
                }

                override fun finished(mediaPlayer: MediaPlayer?) {
                    // Асинхронно переключаем на следующий трек в фоне, чтобы не блокировать LibVLC
                    playerScope.launch {
                        _positionMs.value = 0L
                        next()
                        if (_state.value != PlayerState.PLAYING && _state.value != PlayerState.BUFFERING) {
                            _state.value = PlayerState.IDLE
                        }
                    }
                }

                override fun error(mediaPlayer: MediaPlayer?) {
                    logger.e { "Ошибка воспроизведения VLC MediaPlayer" }
                    _state.value = PlayerState.ERROR
                }

                override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {
                    _positionMs.value = newTime
                }

                override fun lengthChanged(mediaPlayer: MediaPlayer?, newLength: Long) {
                    if (newLength > 0) {
                        _durationMs.value = newLength
                    }
                }

                override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {
                    if (newCache < 100f && _state.value == PlayerState.PLAYING) {
                        _state.value = PlayerState.BUFFERING
                    } else if (newCache >= 100f && _state.value == PlayerState.BUFFERING) {
                        _state.value = PlayerState.PLAYING
                    }
                }
            })

            component
        } catch (e: Throwable) {
            logger.e(e) { "Не удалось инициализировать AudioPlayerComponent VLC: ${e.message}" }
            null
        }
    }

    override fun play(track: Track) {
        playTrackInternal(track, startPositionMs = 0L)
    }

    private fun playTrackInternal(track: Track, startPositionMs: Long) {
        _currentTrack.value = track
        _durationMs.value = track.durationSeconds * 1000L
        _positionMs.value = startPositionMs
        loadedMediaTrackId = track.id
        _state.value = PlayerState.BUFFERING

        if (_queue.value.isEmpty()) {
            _queue.value = listOf(track)
        } else if (_queue.value.none { it.id == track.id }) {
            _queue.value = _queue.value + track
        }

        playerScope.launch {
            val component = playerComponent
            if (component == null) {
                logger.e { "Плеер VLC не инициализирован" }
                _state.value = PlayerState.ERROR
                return@launch
            }

            logger.i { "Запуск воспроизведения '${track.title}' (смещение ${startPositionMs}мс): ${track.streamUrl}" }
            val mediaOptions = if (startPositionMs > 0L) {
                arrayOf(":start-time=${startPositionMs / 1000.0}")
            } else {
                emptyArray()
            }

            val ok = component.mediaPlayer().media().play(track.streamUrl, *mediaOptions)
            if (!ok) {
                logger.e { "Не удалось начать воспроизведение потока VLC" }
                _state.value = PlayerState.ERROR
            }
        }
    }

    override fun setQueue(
        tracks: List<Track>,
        currentTrack: Track?,
        positionMs: Long,
        autoPlay: Boolean
    ) {
        _queue.value = tracks
        val target = currentTrack ?: tracks.firstOrNull() ?: return
        if (autoPlay) {
            playTrackInternal(target, positionMs)
        } else {
            load(target, positionMs)
        }
    }

    override fun load(track: Track, positionMs: Long) {
        _currentTrack.value = track
        _durationMs.value = track.durationSeconds * 1000L
        _positionMs.value = positionMs
        loadedMediaTrackId = null
        _state.value = PlayerState.PAUSED

        if (_queue.value.isEmpty()) {
            _queue.value = listOf(track)
        } else if (_queue.value.none { it.id == track.id }) {
            _queue.value = _queue.value + track
        }
    }

    override fun next() {
        val q = _queue.value
        val cur = _currentTrack.value
        val currentIndex = if (cur != null) q.indexOfFirst { it.id == cur.id } else -1
        if (currentIndex in 0 until q.lastIndex) {
            play(q[currentIndex + 1])
        }
    }

    override fun previous() {
        if (_positionMs.value > 3000L) {
            seekTo(0L)
            return
        }
        val q = _queue.value
        val cur = _currentTrack.value
        val currentIndex = if (cur != null) q.indexOfFirst { it.id == cur.id } else -1
        if (currentIndex > 0) {
            play(q[currentIndex - 1])
        } else {
            seekTo(0L)
        }
    }

    override fun pause() {
        _state.value = PlayerState.PAUSED
        playerScope.launch {
            try {
                playerComponent?.mediaPlayer()?.controls()?.pause()
            } catch (e: Throwable) {
                logger.w(e) { "Ошибка при pause(): ${e.message}" }
            }
        }
    }

    override fun resume() {
        val track = _currentTrack.value ?: return
        playerScope.launch {
            try {
                val component = playerComponent ?: return@launch
                val mp = component.mediaPlayer()
                if (loadedMediaTrackId == track.id && mp.status().state() == State.PAUSED) {
                    _state.value = PlayerState.PLAYING
                    mp.controls().play()
                } else {
                    playTrackInternal(track, _positionMs.value)
                }
            } catch (e: Throwable) {
                logger.w(e) { "Ошибка при resume(): ${e.message}" }
            }
        }
    }

    override fun togglePlayPause() {
        if (_state.value == PlayerState.PLAYING) {
            pause()
        } else if (_currentTrack.value != null) {
            resume()
        }
    }

    override fun stop() {
        _state.value = PlayerState.IDLE
        _positionMs.value = 0L
        loadedMediaTrackId = null
        playerScope.launch {
            try {
                playerComponent?.mediaPlayer()?.controls()?.stop()
            } catch (e: Throwable) {
                logger.w(e) { "Ошибка при stop(): ${e.message}" }
            }
        }
    }

    override fun seekTo(positionMs: Long) {
        _positionMs.value = positionMs
        playerScope.launch {
            try {
                val mp = playerComponent?.mediaPlayer()
                if (mp != null && loadedMediaTrackId == _currentTrack.value?.id) {
                    mp.controls().setTime(positionMs)
                }
            } catch (e: Throwable) {
                logger.w(e) { "Ошибка при seekTo(): ${e.message}" }
            }
        }
    }

    override fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _volume.value = clamped
        playerScope.launch {
            try {
                playerComponent?.mediaPlayer()?.audio()?.setVolume((clamped * 100).toInt())
            } catch (e: Throwable) {
                logger.w(e) { "Ошибка при setVolume(): ${e.message}" }
            }
        }
    }

    override fun release() {
        try {
            playerScope.cancel()
            playerComponent?.release()
        } catch (e: Throwable) {
            logger.w(e) { "Ошибка при освобождении VLC: ${e.message}" }
        }
    }
}
