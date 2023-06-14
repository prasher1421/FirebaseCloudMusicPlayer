package com.prasher.spotifyclone.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prasher.spotifyclone.data.entities.Song
import com.prasher.spotifyclone.exoplayer.MusicServiceConnection
import com.prasher.spotifyclone.exoplayer.isPlayEnabled
import com.prasher.spotifyclone.exoplayer.isPlaying
import com.prasher.spotifyclone.exoplayer.isPrepared
import com.prasher.spotifyclone.other.Constants.MEDIA_ROOT_ID
import com.prasher.spotifyclone.other.Response
import javax.inject.Inject

class MainViewModel @Inject constructor(
    //those dependencies that are injected which are specified in constructor
    private val musicServiceConnection : MusicServiceConnection
)  : ViewModel(){
    private val _mediaItems = MutableLiveData<Response<List<Song>>>()
    val mediaItems : LiveData<Response<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.currentlyPlayingSong
    val playbackState = musicServiceConnection.playbackState


    init {
        _mediaItems.postValue(Response.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object : SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }

                //posting the children after being mapped to each of the song
                _mediaItems.postValue(Response.success(items))

            }
        })
    }

    fun skipToNextSong(){
        musicServiceConnection.transportControls.skipToNext()
    }
    fun skipToPreviousSong(){
        musicServiceConnection.transportControls.skipToPrevious()
    }
    fun seekTo(pos : Long){
        musicServiceConnection.transportControls.seekTo(pos)
    }

    //song we want to toggle and by default we don't want to toggle the song state
    //from play to pause or pause to play
    //or if we directly click on other song just don't pause
    fun playOrToggleSong(mediaItem : Song, toggle : Boolean = false){
        val isPrepared = playbackState.value?.isPrepared ?: false
        if(isPrepared && mediaItem.mediaID ==
            curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {

            //IMPLEMENTATION OF PLAY AND PAUSE BUTTONS

            playbackState.value?.let { playbackState ->
                when{
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            //not pausing when other song is clicked but play that song only
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaID,null)
        }
    }

    // When ViewModel is destroyed just unsubscribe to the MEDIA_ROOT_ID
    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : SubscriptionCallback() {})
    }

}