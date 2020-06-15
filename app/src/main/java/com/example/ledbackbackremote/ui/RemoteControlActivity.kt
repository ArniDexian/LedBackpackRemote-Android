package com.example.ledbackbackremote.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.ledbackbackremote.R
import com.example.ledbackbackremote.databinding.ActivityBtconnectionBinding
import com.example.ledbackbackremote.databinding.ActivityRemoteControlBinding
import com.example.ledbackbackremote.model.RemoteControlViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemoteControlActivity : AppCompatActivity() {

    private val viewModel: RemoteControlViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_control)

        val binding: ActivityRemoteControlBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_remote_control
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    fun onBrightnessUp(view: View) {
        viewModel.brightnessUp()
    }

    fun onBrightnessDown(view: View) {
        viewModel.brightnessDown()
    }

    fun onPrevSprite(view: View) {
        viewModel.prevSprite()
    }

    fun onNextSprite(view: View) {
        viewModel.nextSprite()
    }

    fun onChooseContentText(view: View) {
        viewModel.displayText()
    }

    fun onChooseContentSprite(view: View) {
        viewModel.displaySprite()
    }

    fun onEnterText(view: View) {
        viewModel.sendCustomText()
    }
}