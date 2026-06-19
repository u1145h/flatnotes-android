package com.flatnotes.android.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServerConfigDto(
    @Json(name = "authType") val authType: String,
    @Json(name = "quickAccessHide") val quickAccessHide: Boolean?,
    @Json(name = "quickAccessTitle") val quickAccessTitle: String?,
    @Json(name = "quickAccessTerm") val quickAccessTerm: String?,
    @Json(name = "quickAccessSort") val quickAccessSort: String?,
    @Json(name = "quickAccessLimit") val quickAccessLimit: Int?
)
