package com.example.ledbackbackremote.core.emulation

import com.example.ledbackbackremote.core.DeviceCommunicator
import com.example.ledbackbackremote.core.Package

class BTFakeCommunicator: DeviceCommunicator {
    override val isEnabled: Boolean
        get() = true

    private var listener: ((result: Result<Package>) -> Unit)? = null

    override fun send(data: Package): Boolean {
        listener?.invoke(
            Result.success(data)
        )
        return true
    }

    override fun observeIncomeData(listener: (result: Result<Package>) -> Unit) {
        this.listener = listener
    }
}