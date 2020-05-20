package com.example.ledbackbackremote.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.ledbackbackremote.core.BTConnectionState
import com.example.ledbackbackremote.core.BTService

class BTConnectionViewModel (
    private val service: BTService
): ViewModel() {
    val stateText: LiveData<String>

    init {
        stateText = Transformations.map(service.state) {
            return@map when (it) {
                BTConnectionState.IDLE -> "Idle"
                BTConnectionState.PAIRING -> "Pairing"
                BTConnectionState.CONNECTING -> "Connecting"
                BTConnectionState.CONNECTED -> "Connected"
            }
        }
    }
}