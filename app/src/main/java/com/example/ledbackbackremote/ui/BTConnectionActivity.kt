package com.example.ledbackbackremote.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.ledbackbackremote.R
import com.example.ledbackbackremote.databinding.ActivityBtconnectionBinding
import com.example.ledbackbackremote.model.BTConnectionViewModel
import kotlinx.android.synthetic.main.activity_btconnection.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class BTConnectionActivity : AppCompatActivity() {

    private val viewModel: BTConnectionViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btconnection)
        setSupportActionBar(toolbar)

        val binding: ActivityBtconnectionBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_btconnection
        )
        binding.hostedContent.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    fun openAppAction(view: View) {
        val intent = Intent(this, ItemListActivity::class.java)
        startActivity(intent)
    }
}
