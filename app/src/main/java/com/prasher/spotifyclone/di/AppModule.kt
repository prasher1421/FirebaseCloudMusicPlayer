package com.prasher.spotifyclone.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.prasher.spotifyclone.R
import com.prasher.spotifyclone.adapters.SongAdapter
import com.prasher.spotifyclone.adapters.SwipeSongAdapter
import com.prasher.spotifyclone.exoplayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)//specifies the component
object AppModule {

    @Singleton
    @Provides// for injecting in viewModel
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ) = MusicServiceConnection(context)

    @Singleton
    @Provides
    fun provideSongAdapter(
        glide : RequestManager
    ) = SongAdapter(glide)

    @Singleton
    @Provides
    fun provideSwipeSongAdapter() = SwipeSongAdapter()

    //Same instance is taken everytime
    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
            .diskCacheStrategy(DiskCacheStrategy.DATA)

    )
}