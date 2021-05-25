package com.cc.selectabletextutils

import android.os.Bundle
import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cc.selectable_text_helper.java.SelectableTextHelper

class MainActivity : AppCompatActivity() {

    var tvSelect : TextView? = null
    val selectableTextHelper = SelectableTextHelper()
    var mTouchX = 0
    var mTouchY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSelect = findViewById(R.id.tv_select)
        tvSelect?.setText(R.string.app_name)

        tvSelect?.setOnLongClickListener(OnLongClickListener {
            selectableTextHelper.showSelectView(tvSelect, mTouchX, mTouchY)
            true
        })
        tvSelect?.setOnTouchListener(OnTouchListener { arg0, event ->
            mTouchX = event.x.toInt()
            mTouchY = event.y.toInt()
            false
        })

        tvSelect?.setOnClickListener {
            selectableTextHelper.resetSelectionInfo()
            selectableTextHelper.hideSelectView()
        }
    }

}