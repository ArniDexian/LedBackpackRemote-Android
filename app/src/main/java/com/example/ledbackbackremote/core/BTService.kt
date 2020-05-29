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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.ledbackbackremote.Config
import com.example.ledbackbackremote.utils.Broadcast
import com.example.ledbackbackremote.utils.Broadcasting
import kotlin.properties.Delegates

const val LOG_TAG = "BPRemote"

class BTService(
    private val context: Context,
    private val config: Config,
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter(),
    override val broadcast: Broadcasting<DeviceConnectionService.Delegate> = Broadcast()
): DeviceConnectionService {
    override var state: DeviceConnectionState by Delegates.observable(DeviceConnectionState.IDLE, {_, _, new ->
        broadcast.forEach { it.onStateChanged(new) }
    })
    private set

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

                    if (device.name == config.deviceName) {
                        stopDiscovering()
                        connectDevice(device)
                    }
                }
            }
        }
    }

    private val transferHandler: Handler = object:  Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_READ -> {

                }
                MESSAGE_WRITE -> {

                }
                MESSAGE_TOAST -> {

                }
            }
        }
    }

    init {
        if (bluetoothAdapter == null) {
            state = DeviceConnectionState.DISABLED
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        LocalBroadcastManager.getInstance(context).registerReceiver(btStateReceiver, filter)

        if (bluetoothAdapter?.isEnabled == false) {
            bluetoothAdapter.enable()
        } else {
            onBtConnect()
        }

        // TODO: - so far simulate connection
        Handler().postDelayed({
            state = DeviceConnectionState.PAIRING
        }, 1000)

        Handler().postDelayed({
            state = DeviceConnectionState.CONNECTING
        }, 2000)

        Handler().postDelayed({
            state = DeviceConnectionState.CONNECTED
        }, 4000)
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
        state = DeviceConnectionState.SEARCHING
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
        return pairedDevices?.first { device -> device.name == config.deviceName }?.also {
            Log.d(LOG_TAG, "found paired device $it")
        }
    }

    private fun connectDevice(device: BluetoothDevice) {
        state = DeviceConnectionState.CONNECTING
        connection = BTConnection(device, transferHandler).apply {
            stateDelegate = {
                when (it) {
                    BTConnection.State.CONNECTING -> this@BTService.state = DeviceConnectionState.CONNECTING
                    BTConnection.State.CONNECTED -> {
                        onConnect()
                    }
                    BTConnection.State.DISCONNECTED -> this@BTService.state = DeviceConnectionState.IDLE
                }
            }
            establish()
        }
    }

    private fun onConnect() {
        state = DeviceConnectionState.CONNECTED
    }
}