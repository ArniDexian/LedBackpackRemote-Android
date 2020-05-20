package com.example.ledbackbackremote.core

import androidx.lifecycle.MutableLiveData

enum class BTConnectionState {
    IDLE, PAIRING, CONNECTING, CONNECTED
}
class BTService {
    val state = MutableLiveData<BTConnectionState>()

    init {
        state.value = BTConnectionState.IDLE
    }
}