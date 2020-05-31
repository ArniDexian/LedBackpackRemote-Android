package com.example.ledbackbackremote.core

interface DeviceCommunicator {
    val isEnabled: Boolean

    fun send(data: Package): Boolean
    fun observeIncomeData(listener: (result: Result<Package>) -> Unit)
}

