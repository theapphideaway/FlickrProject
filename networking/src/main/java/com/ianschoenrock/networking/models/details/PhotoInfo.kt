package com.ianschoenrock.networking.models.details

import com.squareup.moshi.Json

data class PhotoInfo(
    val id: String,
    val secret: String,
    val server: String,
    val farm: Int,
    @Json(name = "dateuploaded") val dateUploaded: String,
    @Json(name = "isfavorite") val isFavorite: Int,
    val license: String,
    @Json(name = "safety_level") val safetyLevel: String,
    val rotation: Int,
    val title: PhotoContent,
    val description: PhotoContent,
    val dates: PhotoDates,
    val owner: PhotoOwner,
    val urls: PhotoUrls? = null
)
