package com.prasher.spotifyclone.exoplayer

import android.content.ComponentName
import android.content.Context
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.prasher.spotifyclone.other.Constants.NETWORK_ERROR
import com.prasher.spotifyclone.other.Event
import com.prasher.spotifyclone.other.Response

class MusicServiceConnection(
    context : Context
) {
    //we use mutable live data private which can be changed only from inside of this class
    //and can be observed from other classes


    private val _isConnected = MutableLiveData<Event<Response<Boolean>>>()
    val isConnected : LiveData<Event<Response<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Response<Boolean>>>()
    val networkError : LiveData<Event<Response<Boolean>>> = _networkError

    //player is playing or not
    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState : LiveData<PlaybackStateCompat?> = _playbackState

    //contains meta info about song currently playing
    private val _currentlyPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currentlyPlayingSong : LiveData<MediaMetadataCompat?> = _currentlyPlayingSong

    //to get access to transport controls used to pause play skip
    lateinit var mediaController: MediaControllerCompat

    val transportControls: MediaControllerCompat.TransportControls //we use getter because we cannot initiate mediaController
        get() = mediaController.transportControls


    fun subscribe(parentId : String, callback : MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.subscribe(parentId,callback)
    }
    fun unsubscribe(parentId : String, callback : MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.unsubscribe(parentId,callback)
    }


    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {

        //when connection happened
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken ).apply {
                registerCallback(MediaControllerCallback())
            }

            _isConnected.postValue(Event(Response.success(true)))
        }


        //when it failed
        override fun onConnectionSuspended() {
            _isConnected.postValue(
                Event(Response.error(
                "The connection was suspended", false
            )
                )
            )
        }


        //when it failed
        override fun onConnectionFailed() {
            _isConnected.postValue(
                Event(Response.error(
                    "Couldn't connect to media browser",false
                    )
                )
            )
        }

    }



    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currentlyPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event) {
                NETWORK_ERROR -> _networkError.postValue(
                    Event(
                        Response.error(
                            "Couldn't connect to the server. Please check your Internet Connection.",
                            null
                        )
                    )
                )
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }


    }
}