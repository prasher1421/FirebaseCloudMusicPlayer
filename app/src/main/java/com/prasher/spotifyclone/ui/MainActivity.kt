package com.prasher.spotifyclone.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.RequestManager
import com.prasher.spotifyclone.R
import com.prasher.spotifyclone.adapters.SwipeSongAdapter
import com.prasher.spotifyclone.data.entities.Song
import com.prasher.spotifyclone.exoplayer.isPlaying
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

    private var playbackState : PlaybackStateCompat? = null

    private lateinit var vpSong : ViewPager2
    private lateinit var ivCurSongImage : ImageView
    private lateinit var ivPlayPause : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vpSong = findViewById(R.id.vpSong)
        ivCurSongImage = findViewById(R.id.ivCurSongImage)
        ivPlayPause = findViewById(R.id.ivPlayPause)
        val navHostFragment = findViewById<View>(R.id.navHostFragment)

        subscribeToObservers()

        vpSong.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playbackState?.isPlaying == true){
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                }else{
                    curPlayingSong = swipeSongAdapter.songs[position]
                    glide.load(curPlayingSong?.imageURL).into(ivCurSongImage)
                }
            }
        })

        vpSong.adapter = swipeSongAdapter
        ivPlayPause.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it,true)
            }
        }

        swipeSongAdapter.setOnClickListener(object : SwipeSongAdapter.OnClickListener{
            override fun onClick(position: Int, song: Song) {
                navHostFragment.findNavController().navigate(
                    R.id.globalActionToSongFragment
                )
            }
        })

        navHostFragment.findNavController()
            .addOnDestinationChangedListener{_,destination,_ ->
                when(destination.id){
                    R.id.songFragment ->    hideBottomBar()
                    R.id.homeFragment -> showBottomBar()
                    else -> showBottomBar()
                }
            }

    }

    private fun hideBottomBar(){
        ivCurSongImage.visibility = View.GONE
        vpSong.visibility = View.GONE
        ivPlayPause.visibility = View.GONE
    }

    private fun showBottomBar(){
        ivCurSongImage.visibility = View.VISIBLE
        vpSong.visibility = View.VISIBLE
        ivPlayPause.visibility = View.VISIBLE
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

        mainViewModel.playbackState.observe(this){
            playbackState = it
            ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true)   R.drawable.ic_pause
                else R.drawable.ic_play
            )
        }

        mainViewModel.isConnected.observe(this){
            it?.getContentIfNotHandled()?.let { result->
                when(result.status){

                    Status.ERROR -> Toast.makeText(
                        this,
                        result.message ?: "An Unknown Error occurred",
                        Toast.LENGTH_LONG
                        ).show()

                    else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this){
            it?.getContentIfNotHandled()?.let { result->
                when(result.status){

                    Status.ERROR -> Toast.makeText(
                        this,
                        result.message ?: "An Unknown Error occurred",
                        Toast.LENGTH_LONG
                        ).show()

                    else -> Unit
                }
            }
        }
    }
}