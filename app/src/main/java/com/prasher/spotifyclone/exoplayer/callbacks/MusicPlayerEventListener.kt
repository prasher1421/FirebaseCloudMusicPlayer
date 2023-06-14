package com.prasher.spotifyclone.exoplayer.callbacks

import android.widget.Toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.prasher.spotifyclone.exoplayer.MusicService

class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.Listener {
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        //if player is ready but we don't want to play the song automatically
        if (playbackState == Player.STATE_READY && !playWhenReady){
            musicService.stopForeground(false)//we want to keep the notification
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        //like no internet
        Toast.makeText(musicService,"Some Error occurred", Toast.LENGTH_SHORT).show()
    }


}