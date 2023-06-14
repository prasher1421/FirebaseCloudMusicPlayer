package com.prasher.spotifyclone.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.prasher.spotifyclone.data.remote.MusicDatabase
import com.prasher.spotifyclone.exoplayer.State.*
import com.google.android.exoplayer2.MediaItem as MediaItemExo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
) {

    //CONTAINS METADATA FOR THE MUSIC
    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO){

        state  = STATE_INITIALIZING
        val allSongs = musicDatabase.getAllSongs()//list of songs
        //we need to covert it into media meta data compat


        songs = allSongs.map {song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, song.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, song.mediaID)
                .putString(METADATA_KEY_TITLE,song.title)//title in app
                .putString(METADATA_KEY_DISPLAY_TITLE,song.title)//title in notification
                .putString(METADATA_KEY_DISPLAY_ICON_URI,song.imageURL)
                .putString(METADATA_KEY_MEDIA_URI,song.songURL)
                .putString(METADATA_KEY_ALBUM_ART_URI,song.imageURL)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE,song.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION,song.subtitle)
                .build()
        }

        state = STATE_INITIALIZED
        //this will trigger the setter
    }

    //a single song
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory) : ConcatenatingMediaSource{
        val concatenatingMediaSource = ConcatenatingMediaSource()
        //just an info for exoplayer so that it can play

        songs.forEach{song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(
                    MediaItemExo.fromUri(
                        song.getString(METADATA_KEY_MEDIA_URI).toUri()
                    )
                )
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItem() = songs.map { song->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)//it is not browsable in our case
    }.toMutableList()

    //when we download data from firestore it actually takes time
    //in coroutine we need mechanism to check whether they have finished downloading
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()
    //will help in whenReady() execution

    private var state : State = STATE_CREATED
        set(value) {//change value of state
            if(value == STATE_INITIALIZED || value == STATE_ERROR){
                //during the synchronized thread no other thread can access this thread at same time
                synchronized(onReadyListeners){
                    field = value //field is the current value of the state and value is value

                    onReadyListeners.forEach { listener ->
                        //this will trigger all the listeners with this state
                        listener(state == STATE_INITIALIZED)//to check if it was successfully initialized
                    }
                }
            } else {
                field = value
            }
        }

    //later this function is called to verify whether music is finished loading
    //only execute when music is finished loading
    fun whenReady(action : (Boolean) -> Unit): Boolean{
        if(state == STATE_CREATED || state == STATE_INITIALIZING){
            //then we want to schedule for later
            onReadyListeners += action
            return false
        }else{
            //when music source is ready execute this action
            action(state == STATE_INITIALIZED)
            return true
        }
    }

}

//several states music can be in
enum class State{
    STATE_CREATED,//before we download
    STATE_INITIALIZING,//during download
    STATE_INITIALIZED,//after download
    STATE_ERROR//downloading error
}