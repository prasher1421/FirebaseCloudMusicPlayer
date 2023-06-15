package com.prasher.spotifyclone.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.prasher.spotifyclone.exoplayer.MusicServiceConnection
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey


@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {

    @Provides
    fun provideMainViewModel(musicServiceConnection : MusicServiceConnection) : MainViewModel{
        return MainViewModel(musicServiceConnection)
    }

    @Provides
    fun provideSongViewModel(musicServiceConnection : MusicServiceConnection) : SongViewModel{
        return SongViewModel(musicServiceConnection)
    }
}