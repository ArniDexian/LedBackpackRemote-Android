package com.example.ledbackbackremote

import com.example.ledbackbackremote.core.BTService
import com.example.ledbackbackremote.model.BTConnectionViewModel
import org.koin.dsl.module

var mainModule = module {
    single { BTService() }
    factory { BTConnectionViewModel(get()) }
}