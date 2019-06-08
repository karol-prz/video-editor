package com.kpchuck.videoeditor.views.rangebars

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.SeekBar
import com.appyvet.materialrangebar.RangeBar
import com.kpchuck.videoeditor.R

open class BaseRangeBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
    ): RangeBar(context, attrs, defStyle) {


    init {
        setConnectingLineColor(context.getColor(R.color.accent))
        setSelectorBoundaryColor(context.getColor(R.color.accent))
        setOnlyOnDrag(false)
        setTickHeight(0f)
        setTickInterval(1f)
        tickStart = 0f
        setPinColor(context.getColor(R.color.accent))
        setPinRadius(dpToPx(12f))
        isBarRounded = true
        setTemporaryPins(false)
        setBarWeight(dpToPx(2f))
        invalidate()
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