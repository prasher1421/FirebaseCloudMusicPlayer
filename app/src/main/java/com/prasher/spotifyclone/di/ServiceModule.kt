package com.prasher.spotifyclone.di

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.prasher.spotifyclone.SpotifyApplication
import com.prasher.spotifyclone.data.remote.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


@Module
@InstallIn(ServiceComponent::class)//Has a lifetime just upto when Service is active
object ServiceModule {

    @ServiceScoped
    @Provides
    fun providesMusicDatabase() = MusicDatabase()

    @ServiceScoped //we cannot scope it by singleton but serviceScoped is equivalent
    @Provides
    fun provideAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)//music content
        .setUsage(C.USAGE_MEDIA)//media
        .build()

    @ServiceScoped
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ) = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(audioAttributes,true) //provided by dagger Hilt
        setHandleAudioBecomingNoisy(true)//if user connects headphones it becomes noisy so it will handle by pausing
    }


    //get music source (firebase)
    @ServiceScoped
    @Provides
    fun provideDataSourceFactory(
        @ApplicationContext context: Context
    ) = DefaultDataSourceFactory(context, Util.getUserAgent(context,"Spotify App"))
}