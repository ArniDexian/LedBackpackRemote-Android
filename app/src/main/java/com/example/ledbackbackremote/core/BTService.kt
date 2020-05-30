package com.example.ledbackbackremote.core

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.*
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
                    if (state == DeviceConnectionState.PAIRING) return

                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device.name == config.deviceName) {
                        Log.e(LOG_TAG, "Found device ${device.name}, creating bond")
                        stopDiscovering()
                        state = DeviceConnectionState.PAIRING
                        device.createBond()
                    }
                }
                // FOR Some reason this doesn't work, tried many times
//                BluetoothDevice.ACTION_PAIRING_REQUEST -> {
//                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//                    val type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR)
//                    if (type == BluetoothDevice.PAIRING_VARIANT_PIN) {
//                        Log.e(LOG_TAG, "Setting pin")
//                        device.setPin(config.devicePin.toByteArray())
//                        device.createBond()
//                        abortBroadcast()
//                    }
//                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    when (device.bondState) {
                        BOND_BONDING -> Log.e(LOG_TAG, "Changed bond state to BONDING")
                        BOND_BONDED -> {
                            Log.e(LOG_TAG, "Changed bond state to BONDED")
                            abortDeviceBroadcastReceiver()
                            connectDevice(device)
                        }
                        BOND_NONE -> {
                            Log.e(LOG_TAG, "Changed bond state to BOND_NONE")
                            abortDeviceBroadcastReceiver()
                            state = DeviceConnectionState.PAIRING_FAILED
                        }
                    }
                }
            }
        }
    }

    private fun abortDeviceBroadcastReceiver() {
        context.unregisterReceiver(deviceSearchReceiver)
    }

    private val transferHandler: Handler = object:  Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_READ -> {
                    Log.e(LOG_TAG, "Message is read")
                }
                MESSAGE_WRITE -> {
                    Log.e(LOG_TAG, "Message is written")
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

        val filter = IntentFilter()
        filter.addAction(ACTION_FOUND)
//        filter.addAction(ACTION_PAIRING_REQUEST)
        filter.addAction(ACTION_BOND_STATE_CHANGED)
        filter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY - 1
        context.registerReceiver(deviceSearchReceiver, filter)

        bluetoothAdapter?.startDiscovery()
    }

    private fun stopDiscovering() {
        bluetoothAdapter?.cancelDiscovery()
    }

    private fun findPairedDevice(): BluetoothDevice? {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        return pairedDevices?.firstOrNull { device -> device.name == config.deviceName }?.also {
            Log.d(LOG_TAG, "found paired device $it")
        }
    }

    private fun connectDevice(device: BluetoothDevice) {
        if (state == DeviceConnectionState.CONNECTING) return
        state = DeviceConnectionState.CONNECTING

        Log.e(LOG_TAG, "Connecting to device ${device.name}")

        connection = BTConnection(device, transferHandler).apply {
            stateDelegate = {
                onBtConnectionStateChanged(it, device)
            }
            establish()
        }
    }

    private fun onBtConnectionStateChanged(it: BTConnection.State, device: BluetoothDevice) {
        when (it) {
            BTConnection.State.CONNECTING ->
                this@BTService.state = DeviceConnectionState.CONNECTING
            BTConnection.State.CONNECTED ->
                this@BTService.state = DeviceConnectionState.CONNECTED
            BTConnection.State.DISCONNECTED ->
                this@BTService.state = DeviceConnectionState.DISCONNECTED
            BTConnection.State.CONNECTION_FAILED -> {
                this@BTService.state = DeviceConnectionState.CONNECTION_FAILED
                retryConnect(device)

            }
        }
    }

    private var retryCount: Int = 0
    private fun retryConnect(device: BluetoothDevice) {
        if (retryCount > 3) {
            retryCount = 0
            return
        }

        retryCount += 1
        Log.e(LOG_TAG, "retry to connect in 2s, attempts: $retryCount")

        Handler(Looper.getMainLooper()).postDelayed({
            connectDevice(device)
        }, 2000)
    }
}