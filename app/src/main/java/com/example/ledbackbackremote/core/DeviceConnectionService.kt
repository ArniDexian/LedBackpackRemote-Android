package com.example.ledbackbackremote.core

import com.example.ledbackbackremote.utils.Broadcasting

interface DeviceConnectionService {
    interface Delegate {
        fun onStateChanged(newState: DeviceConnectionState)
    }
    val state: DeviceConnectionState
    val broadcast: Broadcasting<Delegate>

    fun getCommunicator(): DeviceCommunicator
}