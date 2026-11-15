package su.nnedition.kord.player

import kotlinx.coroutines.flow.StateFlow
import su.nnedition.kord.data.model.Track

interface AudioPlayer {
    val currentTrack: StateFlow<Track?>
    val queue: StateFlow<List<Track>>
    val state: StateFlow<PlayerState>
    val positionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>
    val volume: StateFlow<Float>

    fun play(track: Track)
    fun setQueue(tracks: List<Track>, currentTrack: Track? = null, positionMs: Long = 0L, autoPlay: Boolean = true)
    fun load(track: Track, positionMs: Long = 0L)
    fun next()
    fun previous()
    fun pause()
    fun resume()
    fun togglePlayPause()
    fun stop()
    fun seekTo(positionMs: Long)
    fun setVolume(volume: Float)
    fun release()
}
