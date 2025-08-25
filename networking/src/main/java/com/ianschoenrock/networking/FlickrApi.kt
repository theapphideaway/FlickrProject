package com.ianschoenrock.networking

import com.ianschoenrock.networking.models.details.PhotoInfoResponse
import com.ianschoenrock.networking.models.search.PhotoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {
    @GET(".")
    suspend fun searchPhotos(
        @Query("method") method: String = "flickr.photos.search",
        @Query("api_key") apiKey: String = BuildConfig.FLICKR_API_KEY,
        @Query("text") searchText: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int = 20,
        @Query("format") format: String = "json",
        @Query("nojsoncallback") noCallback: Int = 1
    ): PhotoSearchResponse

    @GET(".")
    suspend fun getPhotoInfo(
        @Query("method") method: String = "flickr.photos.getInfo",
        @Query("api_key") apiKey: String = BuildConfig.FLICKR_API_KEY,
        @Query("photo_id") photoId: String,
        @Query("secret") secret: String? = null,
        @Query("format") format: String = "json",
        @Query("nojsoncallback") nojsoncallback: Int = 1
    ): PhotoInfoResponse
}