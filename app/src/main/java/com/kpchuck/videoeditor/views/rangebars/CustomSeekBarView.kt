package com.kpchuck.videoeditor.views.rangebars

import android.content.Context
import android.util.AttributeSet
import android.graphics.*
import android.util.TypedValue
import android.widget.SeekBar


class CustomSeekBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
    ): SeekBar(context, attrs, defStyle) {

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect: RectF
    var showDiff = false
    var currentPosition = 0f
    var secondaryPosition = 0

    init {
        rect = RectF(
            paddingLeft.toFloat(),
            (height/2) - (dpToPx(1f)/2),
            (width - paddingRight).toFloat(),
            height/2 + (dpToPx(1f)/2)
        )
    }

    fun freezeSecondary(){
        showDiff = true
        currentPosition = getCurrentState()
        secondaryPosition = progress
    }

    fun resetSecondary(){
        showDiff = false
        currentPosition = 0f
        secondaryPosition = 0
    }

    private fun getCurrentState(): Float {
        val width = (width
                - paddingLeft
                - paddingRight)
        val thumbPos = (paddingLeft + width * progress).toFloat() / max
        return thumbPos + paddingLeft
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!showDiff) return

        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = Color.GREEN
        paint.isAntiAlias = true

        // Get Height
        val top = (height/2) - (dpToPx(3f)/2)
        val bottom = height/2 + (dpToPx(3f)/2)
        rect.top = top
        rect.bottom = bottom

        // draw seek bar active range line
        if (currentPosition < getCurrentState()) {
            rect.left = currentPosition
            rect.right = getCurrentState()
        } else {
            rect.left = getCurrentState()
            rect.right = currentPosition
        }



        canvas?.drawRect(rect, paint)
    }

    private fun dpToPx(dip: Float): Float {
        val r = resources
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        )
        return px
    }
}