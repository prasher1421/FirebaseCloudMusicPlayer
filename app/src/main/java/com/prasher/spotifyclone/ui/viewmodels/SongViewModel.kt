package com.prasher.spotifyclone.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prasher.spotifyclone.exoplayer.MusicService
import com.prasher.spotifyclone.exoplayer.MusicServiceConnection
import com.prasher.spotifyclone.exoplayer.currentPlaybackPosition
import com.prasher.spotifyclone.other.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SongViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    //we will update them with coroutines
    private val _curSongDuration = MutableLiveData<Long>()
    val curSongDuration : LiveData<Long> = _curSongDuration

    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition : LiveData<Long> = _curPlayerPosition


    init {
        updateCurrentPlayerPosition()
    }

    //could have run it in mainViewModel but would be redundant sometimes
    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while (true){
                val pos = playbackState.value?.currentPlaybackPosition
                if (curPlayerPosition.value != pos){//update it
                    _curPlayerPosition.postValue(pos!!)
                    _curSongDuration.postValue(MusicService.curSongDuration)
                }
                delay(Constants.UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }

}