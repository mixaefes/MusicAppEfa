package com.example.musicapplab.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ART_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_AUTHOR
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import androidx.core.net.toUri
import com.example.musicapplab.data.MusicDataUtil
import com.example.musicapplab.exoplayer.State.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JsonMusicSource @Inject constructor(
    private val musicData: MusicDataUtil
) {
    var songs = emptyList<MediaMetadataCompat>()
  //  suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
     fun fetchMediaData()  {
        state = STATE_INITIALIZING
        val allSongs = musicData.getListOfSongs()
        songs = allSongs!!.map { song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_MEDIA_ID,song.trackUri)
                .putString(METADATA_KEY_ARTIST, song.artist)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_AUTHOR, song.artist)
                .putString(METADATA_KEY_MEDIA_URI, song.trackUri)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.bitmapUri)
                .build()
        }
        state = STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaId(song.getString(METADATA_KEY_MEDIA_ID))
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.getString(METADATA_KEY_TITLE))
            .setIconUri(song.getString(METADATA_KEY_ALBUM_ART_URI).toUri())
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)

    }.toMutableList()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()
    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            return false
        } else {
            action(state == STATE_INITIALIZED)
            return true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}