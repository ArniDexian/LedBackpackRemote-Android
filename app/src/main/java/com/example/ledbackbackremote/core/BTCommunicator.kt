package com.example.ledbackbackremote.core

class BTCommunicator(
    private val connection: BTConnection
): DeviceCommunicator {
    override var isEnabled: Boolean = false
        get() = connection.state == BTConnection.State.CONNECTED
        private set

    override fun send(data: Package): Boolean {
        return connection.write(data.data) && connection.write("\r\n".toByteArray())
    }

    override fun observeIncomeData(listener: (result: Result<Package>) -> Unit) {
        connection.responseListener = { result ->
            listener(result.map { Package(it) })
        }
    }
}