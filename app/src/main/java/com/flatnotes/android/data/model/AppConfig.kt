package com.flatnotes.android.data.model

import org.json.JSONArray
import org.json.JSONObject

data class AppConfig(
    val version: Int = 1,
    val pinnedNotes: List<String> = emptyList()
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("version", version)
            put("pinnedNotes", JSONArray(pinnedNotes))
        }.toString()
    }

    companion object {
        private const val CONFIG_VERSION = 1

        fun fromJson(json: String): AppConfig {
            return try {
                val obj = JSONObject(json)
                val arr = obj.optJSONArray("pinnedNotes")
                val pinned = mutableListOf<String>()
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        pinned.add(arr.getString(i))
                    }
                }
                AppConfig(
                    version = obj.optInt("version", CONFIG_VERSION),
                    pinnedNotes = pinned
                )
            } catch (_: Exception) {
                AppConfig()
            }
        }
    }
}
