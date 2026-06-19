package com.flatnotes.android.data.api

import com.flatnotes.android.data.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface FlatnotesApiService {

    @POST("api/token")
    suspend fun login(@Body request: LoginRequestDto): Response<TokenResponseDto>

    @GET("api/auth-check")
    suspend fun authCheck(): Response<String>

    @GET("api/notes/{title}")
    suspend fun getNote(@Path("title") title: String): Response<NoteDto>

    @POST("api/notes")
    suspend fun createNote(@Body note: NoteCreateDto): Response<NoteDto>

    @PATCH("api/notes/{title}")
    suspend fun updateNote(
        @Path("title") title: String,
        @Body update: NoteUpdateDto
    ): Response<NoteDto>

    @DELETE("api/notes/{title}")
    suspend fun deleteNote(@Path("title") title: String): Response<Unit>

    @GET("api/search")
    suspend fun search(
        @Query("term") term: String,
        @Query("sort") sort: String? = null,
        @Query("order") order: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<SearchResultDto>>

    @GET("api/tags")
    suspend fun getTags(): Response<List<String>>

    @GET("api/config")
    suspend fun getConfig(): Response<ServerConfigDto>

    @Multipart
    @POST("api/attachments")
    suspend fun uploadAttachment(@Part file: MultipartBody.Part): Response<AttachmentResponseDto>

    @GET("api/attachments/{filename}")
    suspend fun downloadAttachment(@Path("filename") filename: String): Response<ResponseBody>

    @GET("health")
    suspend fun healthCheck(): Response<String>
}
