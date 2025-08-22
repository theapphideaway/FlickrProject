package com.ianschoenrock.networking

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class NetworkModule {
    private fun createRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit{
        return Retrofit.Builder()
            .baseUrl(BuildConfig.FLICKR_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }
}