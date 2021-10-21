package com.example.musicapplab.data

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.addAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MusicDataUtil {
    suspend fun getListOfSongs(context: Context): List<Song>? {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val listType = Types.newParameterizedType(List::class.java, Song::class.java)
        val adapter: JsonAdapter<List<Song>> = moshi.adapter(listType)
        val file = "playlist.json"
        val myJson = context.assets.open(file).bufferedReader().use { it.readText() }
        return adapter.fromJson(myJson)
    }
}
