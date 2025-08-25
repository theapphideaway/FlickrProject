package com.ianschoenrock.networking.models.search

data class Photos(
    val page: Int,
    val pages: Int,
    val perpage: Int,
    val total: String,
    val photo: List<Photo>
)
