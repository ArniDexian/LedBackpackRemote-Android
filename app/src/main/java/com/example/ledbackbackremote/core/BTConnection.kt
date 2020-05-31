package com.example.ledbackbackremote.core

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.properties.Delegates

private const val MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"

class BTConnection(
    private val device: BluetoothDevice
) {
    enum class State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        CONNECTION_FAILED
    }

    var state: State by Delegates.observable(State.DISCONNECTED) { _, _, new ->
        Log.d(LOG_TAG,"BTConnection:State changed $new")
        stateListener?.invoke(new)
    }
        private set

    var stateListener: ((State) -> Unit)? = null
    var responseListener: ((Result<ByteArray>) -> Unit)? = null

    private var connectThread: ConnectThread? = null
    private var connectionThread: ConnectedThread? = null

    fun establish() {
        if (state == State.CONNECTING || state == State.CONNECTED) return
        state = State.CONNECTING
        connectThread = ConnectThread(device).apply {
            start()
        }
    }

    fun close() {
        connectThread?.cancel()
        connectThread = null
        connectionThread?.cancel()
        connectionThread = null
    }

    fun write(data: ByteArray): Boolean {
        return connectionThread?.write(data) ?: false
    }

    private fun onConnect(socket: BluetoothSocket) {
        connectionThread = ConnectedThread(socket).apply {
            start()
        }
        state = State.CONNECTED

        write("HI\r\n".toByteArray())
    }

    private fun onConnectionFailed() {
        state = State.CONNECTION_FAILED
    }

    private fun onDisconnect() {
        state = State.DISCONNECTED
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID))
        }

        override fun run() {
            mmSocket?.also { socket ->
                try {
                    Log.d(LOG_TAG, "Trying to open RFCOMM socket")
                    socket.connect()
                    Log.d(LOG_TAG, "RFCOMM socket opened!")
                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
                    onConnect(socket)
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Failed to open RFCOMM socket, error: ${e.message}")
                    onConnectionFailed()
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
                onDisconnect()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(
        private val mmSocket: BluetoothSocket
    ) : Thread() {
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val bufferedReader = BufferedReader(InputStreamReader(mmSocket.inputStream,"US-ASCII"))

        override fun run() {
            while (true) {
                if (!listenForIncomingData()) {
                    if (!mmSocket.isConnected) {
                        onDisconnect()
                        break
                    }

                }
            }
        }

        private fun listenForIncomingData(): Boolean {
            val value = try {
                bufferedReader.readLine()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Input stream was disconnected", e)
                responseListener?.invoke(
                    Result.failure(Throwable("Reading thread is interrupted", e))
                )
                return false
            }

            value?.also {
                Log.d(LOG_TAG, "Received data: ${it}\n")
                responseListener?.invoke(
                    Result.success(it.toByteArray())
                )
            }

            return true
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray): Boolean {
            return try {
                mmOutStream.write(bytes)
                true
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Error occurred when sending data", e)
                false
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
                onDisconnect()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Could not close the connect socket", e)
            }
        }
    }
}