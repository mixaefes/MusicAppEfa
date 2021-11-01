package com.example.musicapplab.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.example.musicapplab.data.Song

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            //it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.iconUri.toString(),
            it.mediaUri.toString(),
        )
    }
}