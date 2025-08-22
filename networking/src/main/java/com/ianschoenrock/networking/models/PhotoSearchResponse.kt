package com.ianschoenrock.networking.models

import com.squareup.moshi.Json

data class PhotoSearchResponse(
    val photos: Photos,
    val stat: String
)
