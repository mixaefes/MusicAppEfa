package com.example.musicapplab.data

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Song(
    val title: String,
    val artist: String,
    val bitmapUri: String,
    val trackUri: String,
)
