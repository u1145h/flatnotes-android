package com.flatnotes.android.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    fun create(serverUrl: String, pathPrefix: String, tokenStorage: TokenStorage): FlatnotesApiService {
        val baseUrl = buildBaseUrl(serverUrl, pathPrefix)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStorage))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(FlatnotesApiService::class.java)
    }

    fun createUnauthenticated(serverUrl: String, pathPrefix: String): FlatnotesApiService {
        val baseUrl = buildBaseUrl(serverUrl, pathPrefix)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(FlatnotesApiService::class.java)
    }

    private fun buildBaseUrl(serverUrl: String, pathPrefix: String): String {
        val url = serverUrl.trimEnd('/')
        val prefix = pathPrefix.trim('/')
        return if (prefix.isNotEmpty()) "$url/$prefix/" else "$url/"
    }
}
