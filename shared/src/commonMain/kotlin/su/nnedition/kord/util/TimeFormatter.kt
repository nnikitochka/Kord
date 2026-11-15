package su.nnedition.kord.util

object TimeFormatter {
    fun formatTrackDuration(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val secStr = if (remainingSeconds < 10) "0$remainingSeconds" else "$remainingSeconds"
        return "$minutes:$secStr"
    }

    fun formatTotalDuration(seconds: Long): String {
        if (seconds <= 0) return "0 мин"
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            hours > 0 && minutes > 0 -> "$hours ч $minutes мин"
            hours > 0 -> "$hours ч"
            else -> "${if (minutes == 0L) 1 else minutes} мин"
        }
    }
}
