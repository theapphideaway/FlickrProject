package com.ianschoenrock.networking.models.details

import com.squareup.moshi.Json

data class PhotoOwner(
    @Json(name = "nsid") val nsId: String,
    val username: String,
    @Json(name = "realname") val realName: String?,
    val location: String?
)
