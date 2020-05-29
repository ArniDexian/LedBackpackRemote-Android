package com.example.ledbackbackremote.model

import android.os.Handler
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.ledbackbackremote.core.DeviceConnectionState
import com.example.ledbackbackremote.core.BTService
import com.example.ledbackbackremote.core.DeviceConnectionService

class BTConnectionViewModel (
    private val service: DeviceConnectionService,
    val stateText: MutableLiveData<String> = MutableLiveData<String>(""),
    val launchButtonVisibility: MutableLiveData<Int> = MutableLiveData<Int>(0)
): DeviceConnectionService.Delegate, ViewModel() {


    init {
        service.broadcast.add(this)
    }

    override fun onStateChanged(newState: DeviceConnectionState) {
        stateText.postValue(when (newState) {
                DeviceConnectionState.IDLE -> "Waiting to connect"
                DeviceConnectionState.PAIRING -> "Pairing..."
                DeviceConnectionState.CONNECTING -> "Connecting..."
                DeviceConnectionState.CONNECTED -> "Connected!"
                DeviceConnectionState.DISABLED -> "Bluetooth is not available :("
                DeviceConnectionState.SEARCHING -> "Searching Backpack"
            }
        )

        launchButtonVisibility.postValue(
            if (newState == DeviceConnectionState.CONNECTED) VISIBLE else INVISIBLE
        )
    }
}