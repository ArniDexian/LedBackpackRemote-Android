package com.example.ledbackbackremote.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.ledbackbackremote.R
import com.example.ledbackbackremote.model.RemoteControlViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemoteControlActivity : AppCompatActivity() {

    private val viewModel: RemoteControlViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_control)
    }

    fun onBrightnessUp(view: View) {

    }

    fun onBrightnessDown(view: View) {

    }

    fun onPrevSprite(view: View) {

    }

    fun onNextSprite(view: View) {

    }

    fun onChooseContentText(view: View) {

    }

    fun onChooseContentSprite(view: View) {

    }

    fun onEnterText(view: View) {

    }
}