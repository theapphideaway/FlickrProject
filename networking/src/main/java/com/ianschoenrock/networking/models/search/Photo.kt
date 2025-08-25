package com.ianschoenrock.networking.models.search

import com.squareup.moshi.Json

data class Photo(
    val id: String,
    val owner: String,
    val secret: String,
    val server: String,
    val farm: Int,
    val title: String,
    @Json(name = "ispublic") val isPublic: Int,
    @Json(name = "isfriend") val isFriend: Int,
    @Json(name = "isfamily") val isFamily: Int
)
