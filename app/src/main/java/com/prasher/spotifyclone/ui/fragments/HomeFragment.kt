package com.prasher.spotifyclone.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prasher.spotifyclone.R
import com.prasher.spotifyclone.adapters.BaseSongAdapter
import com.prasher.spotifyclone.adapters.SongAdapter
import com.prasher.spotifyclone.adapters.SwipeSongAdapter
import com.prasher.spotifyclone.data.entities.Song
import com.prasher.spotifyclone.other.Status
import com.prasher.spotifyclone.ui.viewmodels.MainViewModel
import com.prasher.spotifyclone.ui.viewmodels.MainViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home){


    private var allSongsProgressBar : ProgressBar? = null
    lateinit var rvAllSongs : RecyclerView

    lateinit var mainViewModel : MainViewModel

    @Inject
    lateinit var mainViewModelFactory: MainViewModelFactory

    @Inject
    lateinit var songAdapter : SongAdapter

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allSongsProgressBar = view.findViewById(R.id.allSongsProgressBar)


        mainViewModel = ViewModelProvider(
            requireActivity(),
            mainViewModelFactory
        )[MainViewModel::class.java]


        setupRecyclerView()
        subscribeToObservers()


    }

    private fun setupRecyclerView(){
        rvAllSongs = view?.findViewById(R.id.rvAllSongs)!!
        rvAllSongs.adapter = songAdapter
        rvAllSongs.layoutManager = LinearLayoutManager(requireContext())


        songAdapter.setOnClickListener(object : SongAdapter.OnClickListener{
            override fun onClick(position: Int, song: Song) {
                mainViewModel.playOrToggleSong(
                    song
                )
            }
        } )
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){result ->
            when(result.status){

                Status.SUCCESS -> {
                    allSongsProgressBar?.visibility = View.GONE
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                    }

                }
                Status.LOADING ->{
                    allSongsProgressBar?.visibility = View.VISIBLE
                }
                Status.ERROR -> Unit

            }
        }
    }

}