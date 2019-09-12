package com.kpchuck.videoeditor.controllers

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.kpchuck.videoeditor.R
import com.kpchuck.videoeditor.utils.LongPressHandler
import com.kpchuck.videoeditor.views.rangebars.SingleRowSeekBar

class PropertySeekBarController(rangeBarWrapper: LinearLayout, val durationChange: () -> Unit): SeekBar.OnSeekBarChangeListener{

    private val startDuration = rangeBarWrapper.findViewById<SingleRowSeekBar>(R.id.startDurationSeekBar)
    private val endDuration = rangeBarWrapper.findViewById<SingleRowSeekBar>(R.id.endDurationSeekBar)

    init {
        startDuration.setOnSeekBarChangeListener(this)
        endDuration.setOnSeekBarChangeListener(this)
    }

    fun setMax(max: Int){
        startDuration.max = max
        endDuration.max = max
        endDuration.progress = max.toFloat()
    }

    fun getDuration(): Pair<Int, Int> {
        return Pair(startDuration.progress.toInt(), endDuration.progress.toInt())
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        durationChange()
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
    }
}