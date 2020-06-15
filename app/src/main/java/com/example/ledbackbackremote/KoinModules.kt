package com.example.ledbackbackremote

import com.example.ledbackbackremote.core.BTService
import com.example.ledbackbackremote.core.CommandCommunicator
import com.example.ledbackbackremote.core.DeviceConnectionService
import com.example.ledbackbackremote.core.emulation.BTFakeService
import com.example.ledbackbackremote.model.BTConnectionViewModel
import com.example.ledbackbackremote.model.RemoteControlViewModel
import com.example.ledbackbackremote.utils.isEmulator
import org.koin.dsl.module

private val config = Config(
    "LedBackPack",
    "0305"
)

var mainModule = module {
    single<DeviceConnectionService> {
        if (isEmulator())
            BTFakeService()
        else
            BTService(get(), config)
    }
    factory {
        BTConnectionViewModel(get())
    }
    factory {
        RemoteControlViewModel(
            CommandCommunicator(
                get<DeviceConnectionService>().getCommunicator()
            )
        )
    }
}