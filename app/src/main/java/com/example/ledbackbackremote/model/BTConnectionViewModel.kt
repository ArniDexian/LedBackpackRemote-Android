package com.example.ledbackbackremote.model

import android.os.Handler
import android.view.View.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.ledbackbackremote.core.BTConnectionState
import com.example.ledbackbackremote.core.BTService

class BTConnectionViewModel (
    private val service: BTService
): ViewModel() {
    val stateText: LiveData<String>
    val launchButtonVisibility: LiveData<Int>

    init {
        stateText = Transformations.map(service.state) {
            return@map when (it) {
                BTConnectionState.IDLE -> "Waiting to connect"
                BTConnectionState.PAIRING -> "Pairing..."
                BTConnectionState.CONNECTING -> "Connecting..."
                BTConnectionState.CONNECTED -> "Connected!"
            }
        }
        launchButtonVisibility = Transformations.map(service.state) {
            return@map if (it == BTConnectionState.CONNECTED) VISIBLE else INVISIBLE
        }

        // TODO: - so far simulate connection
        Handler().postDelayed({
            service.state.value = BTConnectionState.PAIRING
        }, 1000)

        Handler().postDelayed({
            service.state.value = BTConnectionState.CONNECTING
        }, 2000)

        Handler().postDelayed({
            service.state.value = BTConnectionState.CONNECTED
        }, 4000)
    }
}