package com.kpchuck.videoeditor.controllers

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.appyvet.materialrangebar.RangeBar
import com.kpchuck.videoeditor.R
import com.kpchuck.videoeditor.utils.LongPressHandler

class RangeBarController(rangeBarWrapper: LinearLayout): RangeBar.OnRangeBarChangeListener{

    private val rangeBar = rangeBarWrapper.findViewById<RangeBar>(R.id.rangeSeekBar)

    private val startIncButton = rangeBarWrapper.findViewById<ImageButton>(R.id.plusStartRangeButton)
    private val startDecButton = rangeBarWrapper.findViewById<ImageButton>(R.id.removeStartRangeButton)
    private val endIncButton = rangeBarWrapper.findViewById<ImageButton>(R.id.plusEndRangeButton)
    private val endDecButton = rangeBarWrapper.findViewById<ImageButton>(R.id.removeEndRangeButton)

    private val startTextView = rangeBarWrapper.findViewById<TextView>(R.id.startTextView)
    private val endTextView = rangeBarWrapper.findViewById<TextView>(R.id.endTextView)

    init {
        rangeBar.setOnRangeBarChangeListener(this)
        // Set up press increments
        startIncButton.setOnClickListener { updatePins(1, 0) }
        startDecButton.setOnClickListener { updatePins(-1, 0) }
        endIncButton.setOnClickListener { updatePins(0, 1) }
        endDecButton.setOnClickListener { updatePins(0, -1) }
        // Set up long presses
        LongPressHandler(startIncButton) { updatePins(1, 0)}
        LongPressHandler(startDecButton) { updatePins(-1, 0 )}
        LongPressHandler(endIncButton) { updatePins(0, 1) }
        LongPressHandler(endDecButton) { updatePins(0, -1) }
        updateText()
    }

    private fun updateText(){
        startTextView.text = "Start: ${rangeBar.leftPinValue}"
        endTextView.text = "End: ${rangeBar.rightPinValue}"
    }

    private fun updatePins(leftPinAdd: Int, rightPinAdd: Int){
        var ladd = leftPinAdd
        if (rangeBar.leftIndex + leftPinAdd < 0)
            ladd = 0
        var radd = rightPinAdd
        if (rangeBar.rightIndex + rightPinAdd > rangeBar.tickEnd)
            radd = rangeBar.tickEnd.toInt()
        rangeBar.setRangePinsByIndices(rangeBar.leftIndex + ladd, rangeBar.rightIndex + radd)
        updateText()
    }

    fun setMax(max: Int){
        rangeBar.tickEnd = max.toFloat()
        updateText()
    }

    override fun onTouchEnded(rangeBar: RangeBar?) {
    }

    override fun onRangeChangeListener(rangeBar: RangeBar?, leftPinIndex: Int, rightPinIndex: Int, leftPinValue: String?, rightPinValue: String?) {
        updateText()
    }

    override fun onTouchStarted(rangeBar: RangeBar?) {
    }
}