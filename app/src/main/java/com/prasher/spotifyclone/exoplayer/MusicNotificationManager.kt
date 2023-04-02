package com.prasher.spotifyclone.exoplayer


import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.prasher.spotifyclone.R
import com.prasher.spotifyclone.other.Constants.NOTIFICATION_CHANNEL_ID
import com.prasher.spotifyclone.other.Constants.NOTIFICATION_ID

class MusicNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: () -> Unit
) {

    private val notificationManager: PlayerNotificationManager


    //TODO("check builder if error occurs")
    init {

        //controls media , get info about currently playing song
        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID,
            DescriptionAdapter(mediaController)
        )
            .setChannelNameResourceId(R.id.notification_channel_name)
            .setChannelDescriptionResourceId(R.id.notification_channel_description)
            .build()
            .apply {
                setSmallIcon(R.drawable.ic_music)
                setMediaSessionToken(sessionToken)
            }

    }


    //just to show this notification
    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        //title of song from media controller
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return mediaController.metadata.description.title.toString()
        }

        //return pending intent
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        //subtitle of song
        override fun getCurrentContentText(player: Player): CharSequence? {
            return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            //icon has to be loaded using glide
            Glide.with(context).asBitmap()
                    //now specify url
                .load(mediaController.metadata.description.iconUri)
                    //
                .into(object : CustomTarget<Bitmap>() {

                    //here we get our fully loaded bitmap
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        //when we asynchronously load image just return null for whole function and return bitmap here
                        callback.onBitmap(resource)
                    }
                    //nothing to be returned
                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                })
            return null
        }
    }
}




















