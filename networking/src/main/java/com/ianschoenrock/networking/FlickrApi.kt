package com.ianschoenrock.networking

import com.ianschoenrock.networking.models.PhotoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {
    @GET(".")
    suspend fun searchPhotos(
        @Query("method") method: String = "flickr.photos.search",
        @Query("api_key") apiKey: String = BuildConfig.FLICKR_API_KEY,
        @Query("text") searchText: String,
        @Query("format") format: String = "json",
        @Query("nojsoncallback") noCallback: Int = 1
    ): PhotoSearchResponse
}