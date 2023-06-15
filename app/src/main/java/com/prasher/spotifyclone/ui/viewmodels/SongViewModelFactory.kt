package com.prasher.spotifyclone.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class SongViewModelFactory @Inject constructor(
    private val songViewModel: SongViewModel
) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return songViewModel as T
    }
}