package com.example.ledbackbackremote.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.ledbackbackremote.R

import kotlinx.android.synthetic.main.activity_btconnection.*

class BTConnectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btconnection)
        setSupportActionBar(toolbar)
    }

    fun onStartAction(view: View) {

    }
}
