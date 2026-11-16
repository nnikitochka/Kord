package su.nnedition.kord.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.time.TimeSource
import kotlinx.coroutines.isActive
import su.nnedition.kord.data.model.Track
import su.nnedition.kord.onHover
import su.nnedition.kord.player.PlayerManager
import su.nnedition.kord.player.PlayerState
import su.nnedition.kord.util.TimeFormatter

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp)
) {
    val player = remember { PlayerManager.instance }

    val currentTrack by player.currentTrack.collectAsState()
    val playerState by player.state.collectAsState()
    val positionMs by player.positionMs.collectAsState()
    val durationMs by player.durationMs.collectAsState()
    val volume by player.volume.collectAsState()

    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }
    var isShuffle by remember { mutableStateOf(false) }
    var isRepeat by remember { mutableStateOf(false) }
    var showVolumeSlider by remember { mutableStateOf(false) }

    var isCardHovered by remember { mutableStateOf(false) }

    var draggingProgress by remember { mutableStateOf<Float?>(null) }
    val isDragging = draggingProgress != null

    val trackDuration = if (durationMs > 0) durationMs else (currentTrack?.durationSeconds ?: 0L) * 1000L

    var displayPosMs by remember { mutableFloatStateOf(0f) }
    var lastVlcPosMs by remember { mutableLongStateOf(0L) }
    var lastVlcTimeMark by remember { mutableStateOf(TimeSource.Monotonic.markNow()) }

    // Synchronize anchor whenever player emits an updated positionMs or track changes
    LaunchedEffect(positionMs, currentTrack?.id) {
        lastVlcPosMs = positionMs
        lastVlcTimeMark = TimeSource.Monotonic.markNow()
        if (playerState != PlayerState.PLAYING) {
            displayPosMs = positionMs.toFloat()
        }
    }

    // High-precision continuous monotonic frame clock with low-pass jitter filtering
    LaunchedEffect(playerState, trackDuration) {
        if (playerState == PlayerState.PLAYING) {
            var lastFrameNanos = withFrameNanos { it }
            while (isActive) {
                withFrameNanos { frameNanos ->
                    val frameDeltaMs = (frameNanos - lastFrameNanos) / 1_000_000f
                    lastFrameNanos = frameNanos

                    val elapsedSinceVlc = lastVlcTimeMark.elapsedNow().inWholeMilliseconds
                    val expectedTarget = (lastVlcPosMs + elapsedSinceVlc).toFloat()
                        .coerceIn(0f, trackDuration.toFloat().coerceAtLeast(1f))

                    val error = expectedTarget - displayPosMs
                    if (abs(error) > 1200f) {
                        displayPosMs = expectedTarget
                    } else {
                        // Smooth proportional correction: advance by real frame delta and gently absorb jitter
                        val correction = error * 0.05f
                        displayPosMs = (displayPosMs + frameDeltaMs + correction)
                            .coerceIn(0f, trackDuration.toFloat().coerceAtLeast(1f))
                    }
                }
            }
        } else {
            displayPosMs = positionMs.toFloat()
        }
    }

    val currentProgressProvider: () -> Float = {
        draggingProgress ?: if (trackDuration > 0) {
            (displayPosMs / trackDuration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    // Colors derived from track cover accent metadata with smooth crossfade
    val yellowAccent = Color(0xFFFAEE00)

    val (targetPlayedColor, targetUnplayedColor) = remember(currentTrack?.accentColor) {
        val baseAccent = parseHexColor(currentTrack?.accentColor)
        if (baseAccent != null) {
            val played = Color(
                red = (baseAccent.red * 0.35f).coerceIn(0.10f, 0.35f),
                green = (baseAccent.green * 0.35f).coerceIn(0.08f, 0.35f),
                blue = (baseAccent.blue * 0.35f).coerceIn(0.08f, 0.35f),
                alpha = 1f
            )
            val unplayed = Color(
                red = (played.red * 0.45f).coerceIn(0.04f, 0.16f),
                green = (played.green * 0.45f).coerceIn(0.03f, 0.16f),
                blue = (played.blue * 0.45f).coerceIn(0.03f, 0.16f),
                alpha = 1f
            )
            played to unplayed
        } else {
            // Default deep dark burgundy if accentColor is missing
            Color(0xFF2B1014) to Color(0xFF140709)
        }
    }

    val playedBgColor by animateColorAsState(
        targetValue = targetPlayedColor,
        animationSpec = tween(durationMillis = 500)
    )
    val unplayedBgColor by animateColorAsState(
        targetValue = targetUnplayedColor,
        animationSpec = tween(durationMillis = 500)
    )

    val defaultGradient = remember(currentTrack?.id, currentTrack?.accentColor) {
        val baseAccent = parseHexColor(currentTrack?.accentColor)
        if (baseAccent != null) {
            listOf(baseAccent, baseAccent.copy(alpha = 0.7f))
        } else {
            val hues = listOf(
                listOf(Color(0xFFE53E3E), Color(0xFFDD6B20)),
                listOf(Color(0xFF3182CE), Color(0xFF63B3ED)),
                listOf(Color(0xFF805AD5), Color(0xFFD53F8C)),
                listOf(Color(0xFF38A169), Color(0xFF48BB78))
            )
            val idx = currentTrack?.id?.hashCode()?.let { (it and 0x7FFFFFFF) % hues.size } ?: 0
            hues[idx]
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onHover(
                onEnter = { isCardHovered = true },
                onExit = { isCardHovered = false }
            )
    ) {
        // 1. CARD BODY (Shadow, Rounded Shape Clip, Background & Content)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(4.dp, shape)
                .clip(shape)
        ) {

            // 1. Dual-tone Background Progress Fill (zero-layout GPU draw)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val progress = currentProgressProvider()
                        val playedWidth = size.width * progress
                        drawRect(
                            color = playedBgColor,
                            topLeft = Offset.Zero,
                            size = Size(playedWidth, size.height)
                        )
                        drawRect(
                            color = unplayedBgColor,
                            topLeft = Offset(playedWidth, 0f),
                            size = Size(size.width - playedWidth, size.height)
                        )
                    }
            )

                // 2. Main Player Content (Track info, Centered controls, Right actions)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT: Track info (Album cover, title, artist, options)
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(defaultGradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentTrack?.title ?: "Ничего не играет",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "Ещё",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .clickable { /* More options */ }
                                )
                            }

                            Text(
                                text = currentTrack?.artist ?: "Выберите трек",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // CENTER: Playback Controls (Dislike, Shuffle, Prev, Yellow Play/Pause, Next, Repeat, Like)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dislike
                        IconButton(
                            onClick = { isDisliked = !isDisliked },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HeartBroken,
                                contentDescription = "Не нравится",
                                tint = if (isDisliked) Color.White else Color.White.copy(alpha = 0.45f),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Shuffle
                        IconButton(
                            onClick = { isShuffle = !isShuffle },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Вперемешку",
                                tint = if (isShuffle) yellowAccent else Color.White.copy(alpha = 0.55f),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Previous
                        IconButton(
                            onClick = { player.previous() },
                            enabled = currentTrack != null,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Предыдущий",
                                tint = if (currentTrack != null) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Big Yellow Play/Pause Button (Signature)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(yellowAccent)
                                .clickable(
                                    enabled = currentTrack != null,
                                    onClick = { player.togglePlayPause() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when (playerState) {
                                PlayerState.BUFFERING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.5.dp,
                                        color = Color(0xFF181818)
                                    )
                                }
                                PlayerState.PLAYING -> {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Пауза",
                                        tint = Color(0xFF181818),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Воспроизвести",
                                        tint = Color(0xFF181818),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }

                        // Next
                        IconButton(
                            onClick = { player.next() },
                            enabled = currentTrack != null,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Следующий",
                                tint = if (currentTrack != null) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Repeat
                        IconButton(
                            onClick = { isRepeat = !isRepeat },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Повтор",
                                tint = if (isRepeat) yellowAccent else Color.White.copy(alpha = 0.55f),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Like
                        IconButton(
                            onClick = { isLiked = !isLiked },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Нравится",
                                tint = if (isLiked) yellowAccent else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // RIGHT: Actions & Volume (Lyrics, Queue, Equalizer, Volume)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lyrics
                        IconButton(
                            onClick = { /* Lyrics */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Article,
                                contentDescription = "Текст песни",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Queue list
                        IconButton(
                            onClick = { /* Queue list */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = "Очередь воспроизведения",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Equalizer / settings
                        IconButton(
                            onClick = { /* Equalizer */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Настройки звука",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Volume icon & slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val volumeIcon = when {
                                volume <= 0f -> Icons.AutoMirrored.Filled.VolumeMute
                                volume < 0.5f -> Icons.AutoMirrored.Filled.VolumeDown
                                else -> Icons.AutoMirrored.Filled.VolumeUp
                            }

                            IconButton(
                                onClick = { showVolumeSlider = !showVolumeSlider },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = volumeIcon,
                                    contentDescription = "Громкость",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = showVolumeSlider,
                                enter = fadeIn() + expandHorizontally(),
                                exit = fadeOut() + shrinkHorizontally()
                            ) {
                                Slider(
                                    value = volume,
                                    onValueChange = { player.setVolume(it) },
                                    modifier = Modifier
                                        .width(76.dp)
                                        .height(18.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = yellowAccent,
                                        activeTrackColor = yellowAccent,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                }
            }

        // 2. TOP INTERACTIVE SCRUBBER / PROGRESS BAR (Hover / Scrubbing State, Unclipped!)
        val isScrubberActive = isCardHovered || isDragging

        AnimatedVisibility(
            visible = isScrubberActive,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val fullWidthPx = constraints.maxWidth.toFloat()

                // 1. Gesture detector + Track Line & Thumb (unclipped by card, only 16dp high at top edge)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-4).dp)
                        .pointerInput(trackDuration) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val fullW = size.width.toFloat()
                                if (fullW > 0f) {
                                    var currentProg = (down.position.x / fullW).coerceIn(0f, 1f)
                                    draggingProgress = currentProg

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break
                                        if (!change.pressed) {
                                            change.consume()
                                            if (trackDuration > 0) {
                                                val targetMs = (currentProg * trackDuration).toLong()
                                                lastVlcPosMs = targetMs
                                                lastVlcTimeMark = TimeSource.Monotonic.markNow()
                                                displayPosMs = targetMs.toFloat()
                                                player.seekTo(targetMs)
                                            }
                                            draggingProgress = null
                                            break
                                        } else {
                                            change.consume()
                                            currentProg = (change.position.x / fullW).coerceIn(0f, 1f)
                                            draggingProgress = currentProg
                                        }
                                    }
                                }
                            }
                        }
                        .drawBehind {
                            val progress = currentProgressProvider()
                            val fullW = size.width
                            val activeW = (progress * fullW).coerceIn(0f, fullW)
                            val trackHeight = 3.dp.toPx()
                            val lineY = 4.dp.toPx()

                            // Inactive track line (grey)
                            drawRect(
                                color = Color.White.copy(alpha = 0.2f),
                                topLeft = Offset(0f, lineY),
                                size = Size(fullW, trackHeight)
                            )

                            // Active progress line (yellow)
                            if (activeW > 0f) {
                                drawRect(
                                    color = yellowAccent,
                                    topLeft = Offset(0f, lineY),
                                    size = Size(activeW, trackHeight)
                                )
                            }

                            // Full circular thumb with sub-pixel float coordinates (never cut off!)
                            val thumbRadius = 5.dp.toPx()
                            val clampedX = activeW.coerceIn(thumbRadius, fullW - thumbRadius)
                            drawCircle(
                                color = yellowAccent,
                                radius = thumbRadius,
                                center = Offset(clampedX, lineY + trackHeight / 2f)
                            )
                        }
                )

                // 2. Floating Current Time Tooltip Badge (e.g. "00:44", floating above card)
                if (fullWidthPx > 0) {
                    val currentDisplaySeconds by remember {
                        derivedStateOf {
                            val prog = draggingProgress ?: if (trackDuration > 0) {
                                (displayPosMs / trackDuration.toFloat()).coerceIn(0f, 1f)
                            } else {
                                0f
                            }
                            (prog * (trackDuration / 1000f)).toLong()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .graphicsLayer {
                                val progress = currentProgressProvider()
                                val pad24Px = 24.dp.toPx()
                                val thumbX = (progress * fullWidthPx).coerceIn(pad24Px, (fullWidthPx - pad24Px).coerceAtLeast(pad24Px))
                                translationX = thumbX - 22.dp.toPx()
                                translationY = (-28).dp.toPx()
                            }
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xE61E1E1E))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formatBadgeTime(currentDisplaySeconds),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 13.sp
                            ),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // 3. Total Duration Badge at top right (e.g. "01:59", floating above card)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = (-28).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xE61E1E1E))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatBadgeTime(trackDuration / 1000L),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 13.sp
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MiniPlayerPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        MiniPlayer()
    }
}

private fun parseHexColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        val clean = hex.removePrefix("#")
        when (clean.length) {
            6 -> Color(clean.toLong(16) or 0xFF000000L)
            8 -> Color(clean.toLong(16))
            else -> null
        }
    } catch (_: Throwable) {
        null
    }
}

private fun formatBadgeTime(totalSeconds: Long): String {
    val sec = totalSeconds.coerceAtLeast(0L)
    val minutes = sec / 60
    val remainingSeconds = sec % 60
    val minStr = if (minutes < 10) "0$minutes" else "$minutes"
    val secStr = if (remainingSeconds < 10) "0$remainingSeconds" else "$remainingSeconds"
    return "$minStr:$secStr"
}
