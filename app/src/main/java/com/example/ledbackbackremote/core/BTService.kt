package com.example.ledbackbackremote.core

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager

enum class BTConnectionState {
    IDLE, SEARCHING, PAIRING, CONNECTING, CONNECTED, DISABLED
}

private const val LED_BP_NAME = "LedBackPack"
private const val LOG_TAG = "BT"
const val CONNECTION_TAG = "BT_CONNECTION"


class BTService(
    private val context: Context
) {
    val state = MutableLiveData<BTConnectionState>()

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var connection: BTConnection? = null

    private val btStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != BluetoothAdapter.ACTION_STATE_CHANGED) return

            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            Log.d(LOG_TAG, "BT state is $state")

            when (state) {
                BluetoothAdapter.STATE_OFF -> Log.d(LOG_TAG, "BT state is off")
                BluetoothAdapter.STATE_ON -> {
                    Log.d(LOG_TAG, "BT state is $state")
                    onBtConnect()
                }
            }
        }
    }

    private val deviceSearchReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//                    val deviceHardwareAddress = device.address // MAC address

                    if (device.name == LED_BP_NAME) {
                        stopDiscovering()
                        connectDevice(device)
                    }
                }
            }
        }
    }

    private val transferHandler: Handler = object:  Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // todo
        }
    }

    init {
        state.value = BTConnectionState.IDLE

        if (bluetoothAdapter == null) {
            state.value = BTConnectionState.DISABLED
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        LocalBroadcastManager.getInstance(context).registerReceiver(btStateReceiver, filter)

        if (bluetoothAdapter?.isEnabled == false) {
            bluetoothAdapter.enable()
        } else {
            onBtConnect()
        }
    }

    private fun onBtConnect() {
        val device = findPairedDevice()
        if (device != null) {
            connectDevice(device)
        } else {
            startDiscovering()
        }
    }

    private fun startDiscovering() {
        state.postValue(BTConnectionState.SEARCHING)
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(deviceSearchReceiver, filter)
        bluetoothAdapter?.startDiscovery()
    }

    private fun stopDiscovering() {
        context.unregisterReceiver(deviceSearchReceiver)
        bluetoothAdapter?.cancelDiscovery()
    }

    private fun findPairedDevice(): BluetoothDevice? {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        return pairedDevices?.first { device -> device.name == LED_BP_NAME }?.also {
            Log.d(LOG_TAG, "found paired device $it")
        }
    }

    private fun connectDevice(device: BluetoothDevice) {
        state.postValue(BTConnectionState.CONNECTING)
        connection = BTConnection(device, transferHandler).apply {
            stateDelegate = {
                when (it) {
                    BTConnection.State.CONNECTING -> this@BTService.state.postValue(BTConnectionState.CONNECTING)
                    BTConnection.State.CONNECTED -> this@BTService.state.postValue(BTConnectionState.CONNECTED)
                    BTConnection.State.DISCONNECTED -> this@BTService.state.postValue(BTConnectionState.IDLE)
                }
            }
            establish()
        }
    }





}