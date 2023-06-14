package com.prasher.spotifyclone.ui

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.prasher.spotifyclone.R
import com.prasher.spotifyclone.adapters.SwipeSongAdapter
import com.prasher.spotifyclone.data.entities.Song
import com.prasher.spotifyclone.exoplayer.toSong
import com.prasher.spotifyclone.other.Status
import com.prasher.spotifyclone.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint//Do this everytime before injecting
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var glide : RequestManager

    private val mainViewModel : MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var curPlayingSong : Song? = null


    private lateinit var vpSong : ViewPager2
    private lateinit var ivCurSongImage : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vpSong = findViewById(R.id.vpSong)
        ivCurSongImage = findViewById(R.id.ivCurSongImage)

        subscribeToObservers()


        vpSong.adapter = swipeSongAdapter


    }

    private fun switchViewPagerToCurrentSong(song: Song){
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if (newItemIndex != -1){
            vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {

                    Status.SUCCESS -> {
                        result.data?.let { songs ->

                            swipeSongAdapter.songs = songs
                            if (songs.isNotEmpty()) {
                                glide
                                    .load((curPlayingSong ?: songs[0]).imageURL)
                                    .into(ivCurSongImage)
                            }

                            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)

                        }
                    }
                    Status.LOADING -> Unit
                    Status.ERROR -> Unit
                }
            }
        }

        mainViewModel.curPlayingSong.observe(this){
            if (it == null) return@observe

            curPlayingSong = it.toSong()
            glide
                .load(curPlayingSong?.imageURL)
                .into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)

        }
    }
}