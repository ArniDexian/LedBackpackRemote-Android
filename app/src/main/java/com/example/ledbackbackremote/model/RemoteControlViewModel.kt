package com.example.ledbackbackremote.model

import androidx.lifecycle.ViewModel
import com.example.ledbackbackremote.core.DeviceCommunicator

class RemoteControlViewModel(
    private val communicator: DeviceCommunicator
) : ViewModel() {

}