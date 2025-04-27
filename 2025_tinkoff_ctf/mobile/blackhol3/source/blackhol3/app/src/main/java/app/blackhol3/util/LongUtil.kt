package app.blackhol3.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Long.Companion.fromByteArray(bytes: ByteArray): Long =
    bytes.foldIndexed(0L) { index, acc, byte ->
        acc + byte.toLong() shl (index * 8)
    }

fun Long.toMessengerFormattedDate(): String {
    val date = Date(this)
    val today = Calendar.getInstance()
    val messageDate =
        Calendar.getInstance().apply {
            time = date
        }

    return when {
        isSameDay(today, messageDate) -> "Сегодня"
        isYesterday(messageDate) -> "Вчера"
        else -> SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(date)
    }
}

fun Long.toMessengerFormattedTime(): String = SimpleDateFormat("HH:mm", Locale("ru")).format(Date(this))

private fun isSameDay(
    cal1: Calendar,
    cal2: Calendar,
): Boolean =
    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

private fun isYesterday(other: Calendar): Boolean {
    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(yesterday, other)
}
