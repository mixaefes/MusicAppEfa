package com.example.musicapplab.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.musicapplab.R
import com.example.musicapplab.data.Song
import com.example.musicapplab.exoplayer.isPlaying
import com.example.musicapplab.exoplayer.toSong
import com.example.musicapplab.other.Status
import com.example.musicapplab.ui.viewmodels.MainViewModel
import com.example.musicapplab.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_song.*

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {
    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()

    private var curPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekBar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()
        ivPlayPauseDetail.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggledSong(it, true)
            }
        }
        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }
        ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
    }

    @SuppressLint("LogNotTimber")
    private fun updateTitleAndSongImage(song: Song) {
        val title = "${song.title} - ${song.artist}"
        tvSongName.text = title
        glide.load(song.bitmapUri).into(ivSongImage)
    }

    @SuppressLint("LogNotTimber")
    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if (curPlayingSong == null && songs.isNotEmpty()) {
                                curPlayingSong = songs[0]
                                Log.i("SongFragment","song = ${songs[0]}")
                                updateTitleAndSongImage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
        mainViewModel.curPlayingSong.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            curPlayingSong = it.toSong()
            updateTitleAndSongImage(curPlayingSong!!)
        }
        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            ivPlayPauseDetail.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_baseline_pause_24 else R.drawable.ic_play
            )
        }
    }
}
