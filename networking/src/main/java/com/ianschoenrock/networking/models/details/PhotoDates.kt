package com.ianschoenrock.networking.models.details

import com.squareup.moshi.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PhotoDates(
    val posted: String,
    val taken: String,
    @Json(name = "takengranularity") val takenGranularity: String
)

fun PhotoDates.getFormattedDateTaken(): String {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = formatter.parse(taken)
        val displayFormatter = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
        date?.let { displayFormatter.format(it) } ?: taken
    } catch (e: Exception) {
        taken
    }
}

fun PhotoDates.getFormattedDatePosted(): String {
    return try {
        val timestamp = posted.toLongOrNull() ?: return posted
        val date = Date(timestamp * 1000L)
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        posted
    }
}
