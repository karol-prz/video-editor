package com.kpchuck.videoeditor.utils

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import java.util.logging.Logger
import kotlin.math.abs


class SwipeDetector : OnTouchListener {
    private var downX: Float = 0f
    private var downY: Float = 0f
    private var upX: Float = 0f
    private var upY: Float = 0f
    var action = Action.None
        private set

    enum class Action {
        LR, // Left to Right
        RL, // Right to Left
        TB, // Top to bottom
        BT, // Bottom to Top
        None // when no action was detected
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                action = Action.None
                return false // allow other events like Click to be processed
            }
            MotionEvent.ACTION_MOVE -> {
                upX = event.x
                upY = event.y

                val deltaX = downX - upX
                val deltaY = downY - upY

                // horizontal swipe detection
                if (abs(deltaX) > MIN_DISTANCE) {
                    // left or right
                    if (deltaX < 0 || deltaX > 0) {
                        Log.d("kpchuck", "Left/right swipe detected")
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                       // action = Action.LR
                        return false
                    }
//                    if (deltaX > 0) {
//                        action = Action.RL
//                        //return true
//                    }
                } else

                // vertical swipe detection
                    if (abs(deltaY) > MIN_DISTANCE) {
                        // top or down
                        if (deltaY < 0 || deltaY > 0) {
                            action = Action.TB
                            Log.d("kpchuck", "Up/down swipe detected")
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                            return false
                        }
                        if (deltaY > 0) {
                            action = Action.BT
                            return false
                        }
                    }
                return true
            }
        }
        return false
    }

    companion object {

        private val MIN_DISTANCE = 1
    }
}