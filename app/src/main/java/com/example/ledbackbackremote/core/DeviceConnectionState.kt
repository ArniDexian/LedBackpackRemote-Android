package com.example.ledbackbackremote.core

enum class DeviceConnectionState {
    IDLE,
    SEARCHING,
    PAIRING,
    CONNECTING,
    CONNECTED,
    PAIRING_FAILED,
    CONNECTION_FAILED,
    DISCONNECTED,
    DISABLED
}