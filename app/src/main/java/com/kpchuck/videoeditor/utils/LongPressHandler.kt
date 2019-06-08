package com.kpchuck.videoeditor.utils

import android.os.Handler
import android.view.MotionEvent
import android.widget.ImageButton

class LongPressHandler(button: ImageButton, val delay: Int = 200, val actionFunction: () -> Unit) {

    private val repeatUpdateHandler = Handler()
    private var keepGoing = false

    init {
        button.setOnLongClickListener {
            keepGoing = true
            repeatUpdateHandler.post(RptUpdater())
            return@setOnLongClickListener false
        }
        button.setOnTouchListener { _, event ->
            if( (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)
                && keepGoing ){
                keepGoing = false
            }
            return@setOnTouchListener false
        }
    }

    inner class RptUpdater: Runnable {

        override fun run() {
            if( keepGoing ){
                actionFunction()
                repeatUpdateHandler.postDelayed( RptUpdater(), delay.toLong() )
            }
        }
    }
}