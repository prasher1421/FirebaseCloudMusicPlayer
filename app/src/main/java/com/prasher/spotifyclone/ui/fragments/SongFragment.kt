package com.prasher.spotifyclone.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.whenResumed
import com.bumptech.glide.RequestManager
import com.prasher.spotifyclone.R
import com.prasher.spotifyclone.data.entities.Song
import com.prasher.spotifyclone.exoplayer.isPlaying
import com.prasher.spotifyclone.exoplayer.toSong
import com.prasher.spotifyclone.other.Status
import com.prasher.spotifyclone.ui.viewmodels.MainViewModel
import com.prasher.spotifyclone.ui.viewmodels.MainViewModelFactory
import com.prasher.spotifyclone.ui.viewmodels.SongViewModel
import com.prasher.spotifyclone.ui.viewmodels.SongViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide : RequestManager

    private lateinit var mainViewModel: MainViewModel
    private lateinit var songViewModel: SongViewModel

    @Inject
    lateinit var mainViewModelFactory: MainViewModelFactory

    @Inject
    lateinit var songViewModelFactory: SongViewModelFactory

    private var curPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekBar : Boolean = true


    private lateinit var tvSongName : TextView
    private lateinit var tvSubtitle : TextView
    private lateinit var tvCurTime : TextView
    private lateinit var tvSongDuration : TextView
    private lateinit var ivSong : ImageView
    private lateinit var ivPlayPauseDetails : ImageView
    private lateinit var ivSkip : ImageView
    private lateinit var ivSkipPrevious : ImageView
    private lateinit var seekBar: SeekBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvSongName = view.findViewById(R.id.tvSongName)
        tvSubtitle = view.findViewById(R.id.tvSubtitleName)
        ivSong = view.findViewById(R.id.ivSongImage)
        ivPlayPauseDetails = view.findViewById(R.id.ivPlayPauseDetail)
        ivSkip = view.findViewById(R.id.ivSkip)
        ivSkipPrevious = view.findViewById(R.id.ivSkipPrevious)
        seekBar = view.findViewById(R.id.seekBar)
        tvCurTime = view.findViewById(R.id.tvCurTime)
        tvSongDuration = view.findViewById(R.id.tvSongDuration)

        mainViewModel = ViewModelProvider(
            this,
            mainViewModelFactory
        )[MainViewModel::class.java]

        songViewModel = ViewModelProvider(
            this,
            songViewModelFactory
        )[SongViewModel::class.java]

        subscribeToObservers()

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            //when the touch is started
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            //when the touch ends
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekBar = true
                }
            }
        })

        ivPlayPauseDetails.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it,true)
            }
        }

        ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

    }


    private fun updateTitleAndSongImage(song : Song){
        tvSongName.text = song.title
        tvSubtitle.text = song.subtitle
        glide.load(song.imageURL).into(ivSong)
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){
            it?.let { response ->
                when(response.status){
                    Status.SUCCESS ->{
                        response.data?.let { songs->
                            if (curPlayingSong == null && songs.isNotEmpty()){
                                curPlayingSong = songs[0]
                                updateTitleAndSongImage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.curPlayingSong.observe(viewLifecycleOwner){
            if (it == null) return@observe
            curPlayingSong = it.toSong()
            updateTitleAndSongImage(curPlayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner){
            playbackState = it
            ivPlayPauseDetails.setImageResource(
                if (playbackState?.isPlaying == true)   R.drawable.ic_pause_white_bg
                else R.drawable.ic_play_white_bg
            )

            seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.curPlayerPosition.observe(viewLifecycleOwner){
            if (shouldUpdateSeekBar){//we need logic for this
                seekBar.progress = it.toInt()
                
                setCurPlayerTimeToTextView(it)
            }
        }

        songViewModel.curSongDuration.observe(viewLifecycleOwner){
            seekBar.max = it.toInt()

            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration.text = dateFormat.format(it - 1800000)

        }

    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text = dateFormat.format(ms - 1800000)
    }
}