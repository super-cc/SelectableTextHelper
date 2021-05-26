package com.cc.selectabletexthelper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cc.selectable_text_helper.java.SelectableTextHelper

class MainActivity : AppCompatActivity() {

    var tvSelect : TextView? = null
    var tvSelectable : TextView? = null
    var selectableTextHelper : SelectableTextHelper? = null
    var mTouchX = 0
    var mTouchY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val operateView = LayoutInflater.from(this).inflate(R.layout.view_select_text_operate, null)
        selectableTextHelper = SelectableTextHelper(operateView, R.drawable.select_text_view_arrow)
        val itCopy = operateView.findViewById<TextView>(R.id.it_copy)
        itCopy.setOnClickListener {
            selectableTextHelper?.copyText()
            selectableTextHelper?.dismiss()
        }
        val itSelectAll = operateView.findViewById<TextView>(R.id.it_select_all)
        itSelectAll.setOnClickListener {
            selectableTextHelper?.selectAll()
        }
        val itCancel = operateView.findViewById<TextView>(R.id.it_cancel)
        itCancel.setOnClickListener {
            selectableTextHelper?.dismiss()
        }

        tvSelect = findViewById(R.id.tv_select)
        tvSelect?.setText(R.string.app_name)

        tvSelect?.setOnLongClickListener(OnLongClickListener {
            selectableTextHelper?.showSelectView(tvSelect, mTouchX, mTouchY)
            true
        })
        tvSelect?.setOnTouchListener(OnTouchListener { arg0, event ->
            mTouchX = event.x.toInt()
            mTouchY = event.y.toInt()
            false
        })

        tvSelect?.setOnClickListener {
            selectableTextHelper?.resetSelectionInfo()
            selectableTextHelper?.hideSelectView()
        }


        tvSelectable = findViewById(R.id.tv_selectable)
        tvSelectable?.setOnLongClickListener(OnLongClickListener {
            selectableTextHelper?.showSelectView(tvSelectable, mTouchX, mTouchY)
            true
        })
        tvSelectable?.setOnTouchListener(OnTouchListener { arg0, event ->
            mTouchX = event.x.toInt()
            mTouchY = event.y.toInt()
            false
        })

        tvSelectable?.setOnClickListener {
            selectableTextHelper?.resetSelectionInfo()
            selectableTextHelper?.hideSelectView()
        }
    }

}