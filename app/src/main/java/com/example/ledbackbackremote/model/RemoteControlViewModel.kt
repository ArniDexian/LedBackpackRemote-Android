package com.example.ledbackbackremote.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ledbackbackremote.core.CommandCommunicator

class RemoteControlViewModel(
    private val communicator: CommandCommunicator
) : ViewModel() {
    val customText: MutableLiveData<String> = MutableLiveData("")

    fun brightnessDown() = onAction {
        it.brightnessDown()
    }

    fun brightnessUp() = onAction {
        it.brightnessUp()
    }

    fun prevSprite() = onAction {
        it.prevSprite()
    }

    fun nextSprite() = onAction {
        it.nextSprite()
    }

    fun displayText() = onAction {
        it.displayText()
    }

    fun displaySprite() = onAction {
        it.displaySprite()
    }

    fun sendCustomText() {
        val text = customText.value ?: return
        onAction {
            if (it.customText(text)) {
                customText.postValue("")
            }
        }
    }

    private fun onAction(action: (CommandCommunicator) -> Unit) {
        if (communicator.device.isEnabled) {
            action(communicator)
        }
    }
}