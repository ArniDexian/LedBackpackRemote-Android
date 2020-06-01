package com.example.ledbackbackremote.core.emulation

import android.os.Handler
import com.example.ledbackbackremote.core.DeviceCommunicator
import com.example.ledbackbackremote.core.DeviceConnectionService
import com.example.ledbackbackremote.core.DeviceConnectionState
import com.example.ledbackbackremote.utils.Broadcast
import com.example.ledbackbackremote.utils.Broadcasting
import kotlin.properties.Delegates

class BTFakeService: DeviceConnectionService {
    override var state: DeviceConnectionState by Delegates.observable(DeviceConnectionState.IDLE, {_, _, new ->
        broadcast.forEach { it.onStateChanged(new) }
    })
        private set

    override val broadcast: Broadcasting<DeviceConnectionService.Delegate> = Broadcast()

    init {
        listOf(
            DeviceConnectionState.SEARCHING,
            DeviceConnectionState.PAIRING,
            DeviceConnectionState.CONNECTING,
            DeviceConnectionState.CONNECTED
        ).forEachIndexed { i, istate ->
            Handler().postDelayed({
                state = istate
            }, i.toLong() * 300)
        }
    }

    override fun getCommunicator(): DeviceCommunicator {
        return BTFakeCommunicator()
    }
}