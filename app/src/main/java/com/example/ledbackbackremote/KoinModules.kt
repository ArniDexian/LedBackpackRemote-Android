package com.example.ledbackbackremote

import com.example.ledbackbackremote.core.BTService
import com.example.ledbackbackremote.core.DeviceConnectionService
import com.example.ledbackbackremote.model.BTConnectionViewModel
import org.koin.dsl.module

private val config = Config(
    "LedBackPack",
    "0305"
)

var mainModule = module {
    single<DeviceConnectionService> { BTService(get(), config) }
    factory { BTConnectionViewModel(get()) }
}