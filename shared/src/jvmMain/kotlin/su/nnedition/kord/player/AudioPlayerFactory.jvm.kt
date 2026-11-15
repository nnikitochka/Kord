package su.nnedition.kord.player

actual fun createAudioPlayer(): AudioPlayer = DesktopAudioPlayer()
