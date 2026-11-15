package su.nnedition.kord.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import su.nnedition.kord.data.model.Track

class NoOpAudioPlayer : AudioPlayer {
    override val currentTrack: StateFlow<Track?> = MutableStateFlow<Track?>(null).asStateFlow()
    override val queue: StateFlow<List<Track>> = MutableStateFlow<List<Track>>(emptyList()).asStateFlow()
    override val state: StateFlow<PlayerState> = MutableStateFlow(PlayerState.IDLE).asStateFlow()
    override val positionMs: StateFlow<Long> = MutableStateFlow(0L).asStateFlow()
    override val durationMs: StateFlow<Long> = MutableStateFlow(0L).asStateFlow()
    override val volume: StateFlow<Float> = MutableStateFlow(1.0f).asStateFlow()

    override fun play(track: Track) {}
    override fun setQueue(tracks: List<Track>, currentTrack: Track?, positionMs: Long, autoPlay: Boolean) {}
    override fun load(track: Track, positionMs: Long) {}
    override fun next() {}
    override fun previous() {}
    override fun pause() {}
    override fun resume() {}
    override fun togglePlayPause() {}
    override fun stop() {}
    override fun seekTo(positionMs: Long) {}
    override fun setVolume(volume: Float) {}
    override fun release() {}
}
