package com.flatnotes.android.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResultDto(
    @Json(name = "title") val title: String,
    @Json(name = "lastModified") val lastModified: Double,
    @Json(name = "score") val score: Float?,
    @Json(name = "titleHighlights") val titleHighlights: String?,
    @Json(name = "contentHighlights") val contentHighlights: String?,
    @Json(name = "tagMatches") val tagMatches: List<String>?
)
