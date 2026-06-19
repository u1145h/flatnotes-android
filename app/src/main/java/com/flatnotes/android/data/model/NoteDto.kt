package com.flatnotes.android.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NoteDto(
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String?,
    @Json(name = "lastModified") val lastModified: Double
)

@JsonClass(generateAdapter = true)
data class NoteCreateDto(
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String?
)

@JsonClass(generateAdapter = true)
data class NoteUpdateDto(
    @Json(name = "newTitle") val newTitle: String?,
    @Json(name = "newContent") val newContent: String?
)
