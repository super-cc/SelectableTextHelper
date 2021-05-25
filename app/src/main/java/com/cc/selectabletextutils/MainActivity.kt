package com.cc.selectabletextutils

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    var tvSelect : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSelect = findViewById(R.id.tv_select)
        tvSelect?.setOnLongClickListener {
            showSelectPop()
            true
        }
    }

    fun showSelectPop() {

    }

}