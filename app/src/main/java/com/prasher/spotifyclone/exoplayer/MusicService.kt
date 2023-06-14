package com.prasher.spotifyclone.exoplayer

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.prasher.spotifyclone.exoplayer.callbacks.MusicPlaybackPreparer
import com.prasher.spotifyclone.exoplayer.callbacks.MusicPlayerEventListener
import com.prasher.spotifyclone.exoplayer.callbacks.MusicPlayerNotificationListener
import com.prasher.spotifyclone.other.Constants.MEDIA_ROOT_ID
import com.prasher.spotifyclone.other.Constants.NETWORK_ERROR
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private lateinit var musicNotificationManager: MusicNotificationManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    //+ means it has merged properties of Main Dispatcher and serviceJob both

    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var mediaSessionConnector : MediaSessionConnector

    private lateinit var musicPlayerEventListener : MusicPlayerEventListener

    //just to tell whether music is in foreground or not
    var isForegroundService = false

    private var curPlayingSong : MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    companion object{
        var curSongDuration = 0L
            private  set //only change value in service but can access outside
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        //for notification
        val activityIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0,it, FLAG_MUTABLE)
        }

        //comes with a token
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }


        sessionToken = mediaSession.sessionToken

        //Here managing begins
        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ){//4th parameter lambda function
            //called when song switches

            curSongDuration = exoPlayer.duration

        }

        //Preparer setup
        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource){
            curPlayingSong = it


            preparePlayer(
                    firebaseMusicSource.songs,
                    it,
                    true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)//injected by dagger hilt

        musicPlayerEventListener = MusicPlayerEventListener(this)

        exoPlayer.addListener(musicPlayerEventListener)

        musicNotificationManager.showNotification(exoPlayer)

    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession){
        //calling next song from firebase
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs : List<MediaMetadataCompat>,
        itemToPlay : MediaMetadataCompat?,
        playNow : Boolean
    ){
        val curSongIndex = if (curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        CoroutineScope(Dispatchers.Main).launch {
            exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
            exoPlayer.seekTo(curSongIndex, 0)
            exoPlayer.playWhenReady = playNow
        }
         // start that song which is selected

        //usually when the app opens we don't play the songs by ourself but wait for user to play song
        //so we generally set playNow false
        //when user clicks then we set it true

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

            //to release resources
        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        //stops playing with this
        exoPlayer.stop()
    }


    //MediaBrowserCompat has several browsable objects like playlists albums
    //here we can also decline client requests but it's of no use
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID,null)
    }



    //doesn't just has a list of songs and to be played but has playlists albums
    //can be thought as a file manager
    //like recommender system
    override fun onLoadChildren(
        parentId: String,//id of the playlists to which client subscribes
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>//list of playable song or browsable
    ) {
        when(parentId){
            MEDIA_ROOT_ID -> {
                val resultsSent = firebaseMusicSource.whenReady {isInitialized ->
                    if (isInitialized){
                        result.sendResult(firebaseMusicSource.asMediaItem())
                        if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()) {

                            preparePlayer(
                                firebaseMusicSource.songs,
                                firebaseMusicSource.songs[0],
                                false
                            )
                            isPlayerInitialized = true
                        }
                    }else{

                        mediaSession.sendSessionEvent(NETWORK_ERROR , null)

                        result.sendResult(null)
                    }
                }

                //onLoadChildren will be called early when ou player is not ready so we
                //need to check if results have been sent or not
                if (!resultsSent){
                    result.detach()//check for later part
                }
            }
        }
    }
}