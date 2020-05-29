package com.example.ledbackbackremote.utils

import java.lang.ref.WeakReference

interface Broadcasting<T> {
    fun add(listener: T)
    fun remove(listener: T)
    fun forEach(callback: (T)-> Unit)
}

class Broadcast<T>: Broadcasting<T> {
    private var listeners = mutableListOf<WeakReference<T>>()

    override fun add(listener: T) {
        if (listeners.find {  it.get() === listener } != null) return
        listeners.add(WeakReference(listener))
    }

    override fun remove(listener: T) {
        listeners.removeIf { it.get() === listener }
    }

    override fun forEach(callback: (T) -> Unit) {
        var hasNulls = false
        listeners.forEach {
            val value = it.get()
            if (value == null) {
                hasNulls = true
            } else {
                callback(value)
            }
        }

        if (hasNulls) {
            clearNulls()
        }
    }

    private fun clearNulls() {
        listeners.removeIf { it.get() == null }
    }
}