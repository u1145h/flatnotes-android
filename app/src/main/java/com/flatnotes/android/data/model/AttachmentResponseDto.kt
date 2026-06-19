package com.flatnotes.android.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttachmentResponseDto(
    @Json(name = "filename") val filename: String,
    @Json(name = "url") val url: String
)
