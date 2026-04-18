package com.smusic.app.presentation.navigation

import androidx.lifecycle.ViewModel
import com.smusic.app.player.SMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    val serviceConnection: SMusicServiceConnection,
) : ViewModel() {
    init {
        serviceConnection.connect()
    }

    override fun onCleared() {
        serviceConnection.saveQueueState()
        super.onCleared()
    }
}
