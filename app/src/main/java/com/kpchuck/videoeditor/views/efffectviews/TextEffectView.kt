package com.kpchuck.videoeditor.views.efffectviews

import android.content.Context
import android.graphics.Bitmap
import android.preference.PreferenceManager
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.view.get
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kpchuck.videoeditor.R
import com.kpchuck.videoeditor.controllers.AttributeViewController
import com.kpchuck.videoeditor.controllers.RangeBarController
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint


class TextEffectView(override val context: Context, override val setFrame: (position: Int) -> Unit, override val dimens: Pair<Int, Int>): BaseEffectView {

    override val rootView: CardView = LayoutInflater.from(context).inflate(R.layout.property_view, null) as CardView
    override val attributes: Array<String>
        get() = arrayOf("Size", "Position X", "Position Y", "Opacity")
    override val attributeController: AttributeViewController
    override val rangeBarController: RangeBarController

    private val propertyInput: TextInputEditText
    private val useKeyFrames: Boolean = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("USE_KEYFRAMES", false)

    init {
        val inputLayout = genPropertyInput() as TextInputLayout
        rootView.findViewById<LinearLayout>(R.id.effectSelectorWrapper).addView(inputLayout, 1)
        propertyInput = (inputLayout[0] as FrameLayout)[0] as TextInputEditText

        val attributeWrapper = rootView.findViewById<LinearLayout>(R.id.attributeWrapper)
        attributeController = AttributeViewController(attributeWrapper, context, attributes, useKeyFrames)

        val rangeBarWrapper = rootView.findViewById<LinearLayout>(R.id.rangeSeekBarWrapper)
        rangeBarController = RangeBarController(rangeBarWrapper)
    }

    override fun getFrame(position: Int): Bitmap? {
        val duration = getDuration()
        if (position < duration.first)
            return null
        val dest = Bitmap.createBitmap(dimens.first, dimens.second, Bitmap.Config.ARGB_8888)
        val yourText = getTitle()
        val attributes = attributeController.getAttributesAt(0)

        val cs = Canvas(dest)
        val tPaint = Paint()
        tPaint.textSize = attributes["Size"]!!.toFloat()
        tPaint.color = Color.WHITE
        tPaint.style = Paint.Style.FILL
        val height = tPaint.measureText("yY")
        val width = tPaint.measureText(yourText)
        val x_coord = (dimens.first - width) / 2
        cs.drawText(yourText, x_coord, height, tPaint)
        return dest
    }

    override fun genPropertyInput(): View {
        val layout = TextInputLayout(context)
        layout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        layout.setPadding(dpToPx(6f).toInt(), 0, dpToPx(6f).toInt(), 0)
        val editText = TextInputEditText(context)
        editText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        editText.hint = "Enter text here"
        layout.addView(editText)
        return layout
    }

    override fun getButtonTitle(): String {
        val title = getTitle()
        if (title.isEmpty())
            return "Text Effect"
        return "Text Effect: ${getTitle()}"
    }

    override fun getKeyFrames(): ArrayMap<Int, ArrayMap<String, Int>> {
        val array = ArrayMap<Int, ArrayMap<String, Int>>()
        if (useKeyFrames){}
        else {
            val duration = getDuration()
            array[duration.first] = attributeController.getAttributesAt(0)
            array[duration.second] = attributeController.getAttributesAt(1)
        }
        return array
    }

    fun getTitle(): String {
        return propertyInput.text.toString()
    }
}