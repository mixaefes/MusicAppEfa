package com.example.musicapplab.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.addAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import javax.inject.Inject

class MusicDataUtil @Inject constructor(
    private val context: Context
) {
     @SuppressLint("LogNotTimber")
     fun getListOfSongs(): List<Song>? {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val listType = Types.newParameterizedType(List::class.java, Song::class.java)
        val adapter: JsonAdapter<List<Song>> = moshi.adapter(listType)
        val file = "playlist.json"
        val myJson = context.assets.open(file).bufferedReader().use { it.readText() }
         Log.i("MusicDataUtil","List of my songs: ${adapter.fromJson(myJson)}")
        return adapter.fromJson(myJson)
    }
}
