package com.kpchuck.videoeditor.views.efffectviews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.ArrayMap
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import com.kpchuck.videoeditor.R
import com.kpchuck.videoeditor.controllers.AttributeViewController
import com.kpchuck.videoeditor.controllers.PropertySeekBarController
import android.util.Log
import com.kpchuck.Drawer
import com.kpchuck.Extrapolator
import com.kpchuck.videoeditor.views.rangebars.SingleRowSeekBar
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.absoluteValue


interface BaseEffectView {

    val context: Context
    val rootView: CardView
    val attributeController: AttributeViewController
    val propertySeekBarController: PropertySeekBarController
    var attributeChangeListener: AttributeChangeListener?
    val keyFrameAttrs: TreeMap<Int, ArrayMap<String, Float>>
    val defaultAttrs: HashMap<String, Int>
    var extrapolatorKeys: ArrayMap<String, ArrayMap<Int, Float>>

    fun getButtonTitle(): String
    fun getKeyFrames(): TreeMap<Int, ArrayMap<String, Float>>
    fun genPropertyInput(): View
    fun getFrame(position: Int): Bitmap?
    fun getDrawer(): Drawer

    companion object {
        const val SIZE = "Size"
        const val OPACITY = "Opacity"
        const val POSITION_X = "Position X (Percent)"
        const val POSITION_Y = "Position Y (Percent)"
        const val ROTATION = "Rotation (Percent)"
        const val SIZE_PERCENT = "Size (Percent)"
    }

    fun getExtrapolatorFor(attr: String): Extrapolator {
        return Extrapolator(Extrapolator.Effect.Linear, defaultAttrs[attr]!!.toFloat(), extrapolatorKeys[attr]!!)
    }

    fun getDuration(): Pair<Int, Int> {
        return propertySeekBarController.getDuration()
    }

    fun setShowing(removeView: (view: CardView) -> Unit){
        val backButton = rootView.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener { removeView(rootView) }
    }

    fun setEnd(position: Int){
        propertySeekBarController.setMax(position)
    }

    fun dpToPx(dip: Float): Float {
        val r = context.resources
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        )
        return px
    }

    fun genExtrapolatorKeys(){
        // Need to loop through keyframeattrs TreeMap<Int, ArrayMap<String, Float>>()
        // And rearange according to string: <Int, Float>
        val keys = ArrayMap<String, ArrayMap<Int, Float>>()
        for (attr in keyFrameAttrs[keyFrameAttrs.firstKey()]!!.keys){
            val timeValues = ArrayMap<Int, Float>()
            for (time in keyFrameAttrs.keys){
                timeValues[time] = keyFrameAttrs[time]!![attr]!!
            }
            keys[attr] = timeValues
        }
        extrapolatorKeys = keys
    }

    interface AttributeChangeListener {
        fun onAttributeChange(view: BaseEffectView)
    }

}