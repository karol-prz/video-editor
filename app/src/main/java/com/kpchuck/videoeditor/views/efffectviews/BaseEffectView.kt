package com.kpchuck.videoeditor.views.efffectviews

import android.content.Context
import android.graphics.Bitmap
import android.util.ArrayMap
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.kpchuck.videoeditor.R
import com.kpchuck.videoeditor.controllers.AttributeViewController
import com.kpchuck.videoeditor.controllers.RangeBarController
import com.kpchuck.videoeditor.views.rangebars.BaseRangeBar

interface BaseEffectView {

    val context: Context
    val rootView: CardView
    val attributes: Array<String>
    val attributeController: AttributeViewController
    val rangeBarController: RangeBarController
    val setFrame: (position: Int) -> Unit
    val dimens: Pair<Int, Int>

    fun getButtonTitle(): String
    fun getKeyFrames(): ArrayMap<Int, ArrayMap<String, Int>>
    fun genPropertyInput(): View
    fun getFrame(position: Int): Bitmap?

    fun getDuration(): Pair<Int, Int> {
        val rangeBar = rootView.findViewById<BaseRangeBar>(R.id.rangeSeekBar)
        return Pair(rangeBar.leftIndex, rangeBar.rightIndex)
    }

    fun setShowing(removeView: (view: CardView) -> Unit){
        val backButton = rootView.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener { removeView(rootView) }
    }

    fun setEnd(position: Int){
        rangeBarController.setMax(position)
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

}