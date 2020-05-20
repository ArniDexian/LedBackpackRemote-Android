package com.example.ledbackbackremote.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ledbackbackremote.R
import com.example.ledbackbackremote.model.BTConnectionViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

import kotlinx.android.synthetic.main.activity_btconnection.*

class BTConnectionActivity : AppCompatActivity() {

    private val viewModel: BTConnectionViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btconnection)
        setSupportActionBar(toolbar)
    }
}
