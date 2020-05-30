package com.example.ledbackbackremote.core

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*
import kotlin.properties.Delegates

const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

private const val MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"

class BTConnection(
    private val device: BluetoothDevice,
    private val transferHandler: Handler
) {
    enum class State {
        DISCONNECTED, CONNECTING, CONNECTED, CONNECTION_FAILED
    }

    var state: State by Delegates.observable(State.DISCONNECTED) { _, _, new ->
        Log.e(LOG_TAG,"BTConnection:State changed $new")
        stateDelegate?.invoke(new)
    }
        private set

    var stateDelegate: ((State) -> Unit)? = null

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

    private fun onConnect(socket: BluetoothSocket) {
        connectionThread = ConnectedThread(socket).apply {
            start()
        }
        state = State.CONNECTED

        connectionThread?.write("hi pirog\r\n".toByteArray())
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
            mmSocket?.use { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                try {
                    Log.e(LOG_TAG, "Trying to open RFCOMM socket")
                    socket.connect()
                    Log.e(LOG_TAG, "RFCOMM socket opened!")
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
//                cancel()
            }
        }
    }

    private inner class ConnectedThread(
        private val mmSocket: BluetoothSocket
    ) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
//            while (true) {
//                // Read from the InputStream.
//                numBytes = try {
//                    mmInStream.read(mmBuffer)
//                } catch (e: IOException) {
//                    Log.d(LOG_TAG, "Input stream was disconnected", e)
//                    break
//                }
//
//                // Send the obtained bytes to the UI activity.
//                val readMsg = transferHandler.obtainMessage(
//                    MESSAGE_READ, numBytes, -1,
//                    mmBuffer
//                )
//                readMsg.sendToTarget()
//            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = transferHandler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                transferHandler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = transferHandler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer
            )
            writtenMsg.sendToTarget()
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