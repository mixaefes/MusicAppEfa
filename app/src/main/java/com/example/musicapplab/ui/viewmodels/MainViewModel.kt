package com.example.musicapplab.ui.viewmodels

import android.annotation.SuppressLint
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicapplab.Constants.MEDIA_ROOT_ID
import com.example.musicapplab.data.Song
import com.example.musicapplab.exoplayer.MusicServiceConnection
import com.example.musicapplab.exoplayer.isPlayEnabled
import com.example.musicapplab.exoplayer.isPlaying
import com.example.musicapplab.exoplayer.isPrepared
import com.example.musicapplab.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
) : ViewModel(){
    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){
            @SuppressLint("LogNotTimber")
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map{
                    Log.i("MainViewModel","${it.description.iconUri} and ${it.description.iconBitmap}")
                    Song(
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.iconUri.toString(),
                        it.description.mediaUri.toString(),
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }
    fun skipToNextSong(){
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong(){
        musicServiceConnection.transportControls.skipToPrevious()
    }

fun playOrToggledSong(mediaItem:Song, toggle:Boolean = false){
    val isPrepared = playbackState.value?.isPrepared ?: false
    if(isPrepared && mediaItem.title ==
        curPlayingSong?.value?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)){
        playbackState.value?.let { playbackState ->
            when{
                playbackState.isPlaying -> if(toggle) musicServiceConnection.transportControls.pause()
                playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                else -> Unit
            }
        }
    } else{
        musicServiceConnection.transportControls.playFromMediaId(mediaItem.trackUri,null)
    }
}
    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){})
    }
}