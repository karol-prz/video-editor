package com.kpchuck.videoeditor.views.efffectviews

import android.content.Context
import android.graphics.*
import android.text.Editable
import android.text.TextWatcher
import android.util.ArrayMap
import android.util.Size
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
import com.kpchuck.videoeditor.controllers.PropertySeekBarController
import android.widget.ImageButton
import androidx.core.view.setPadding
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.kpchuck.Drawer
import com.kpchuck.Extrapolator
import com.kpchuck.TextDrawer
import com.kpchuck.videoeditor.EffectVideoBinder
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView.Companion.OPACITY
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView.Companion.POSITION_X
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView.Companion.POSITION_Y
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView.Companion.ROTATION
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView.Companion.SIZE
import java.util.*
import kotlin.collections.HashMap




class TextEffectView(override val context: Context, val effectVideoBinder: EffectVideoBinder): BaseEffectView {


    override val rootView: CardView = LayoutInflater.from(context).inflate(R.layout.property_view, null) as CardView
    override val attributeController: AttributeViewController
    override val propertySeekBarController: PropertySeekBarController
    override var attributeChangeListener: BaseEffectView.AttributeChangeListener? = null
    private val propertyInput: TextInputEditText
    override val keyFrameAttrs = TreeMap<Int, ArrayMap<String, Float>>()
    override val defaultAttrs: HashMap<String, Int> = hashMapOf(
        Pair(SIZE, 30),
        Pair(POSITION_X, 50),
        Pair(POSITION_Y, 100),
        Pair(OPACITY, 100),
        Pair(ROTATION, 0)
    )
    override var extrapolatorKeys = ArrayMap<String, ArrayMap<Int, Float>>()

    var currentColor = Color.WHITE

    init {
        val inputLayout = genPropertyInput() as TextInputLayout
        val wrapper = rootView.findViewById<LinearLayout>(R.id.effectSelectorWrapper)
        wrapper.addView(inputLayout)
        wrapper.addView(createColorPicker())
        propertyInput = (inputLayout[0] as FrameLayout)[0] as TextInputEditText
        propertyInput.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                attributeChangeListener?.onAttributeChange(this@TextEffectView)
            }
        })

        val arrayMap = ArrayMap<String, Float>()
        for (i in defaultAttrs.keys.sorted())
            arrayMap[i] = defaultAttrs[i]!!.toFloat()
        keyFrameAttrs[0] = arrayMap
        //val attributeWrapper = rootView.findViewById<LinearLayout>(R.id.attributeWrapper)
        attributeController = AttributeViewController(rootView, context, keyFrameAttrs, defaultAttrs.keys.sorted().reversed(), effectVideoBinder)
            {attributeChangeListener?.onAttributeChange(this)}

        val rangeBarWrapper = rootView.findViewById<LinearLayout>(R.id.rangeSeekBarWrapper)
        propertySeekBarController = PropertySeekBarController(rangeBarWrapper) {attributeChangeListener?.onAttributeChange(this)}
    }

    override fun getFrame(position: Int): Bitmap? {
        val duration = getDuration()
        if (position < duration.first || position > duration.second)
            return null
        val dimens = effectVideoBinder.dimens
        val dest = Bitmap.createBitmap(dimens.first, dimens.second, Bitmap.Config.ARGB_8888)
        val cs = Canvas(dest)
        val textDrawer = getDrawer()
        textDrawer.draw(cs, position)
        return dest
    }

    override fun getDrawer(): Drawer {
        val dimens = effectVideoBinder.dimens
        val yourText = getTitle()
        genExtrapolatorKeys()
        return TextDrawer(yourText, currentColor, getExtrapolatorFor(SIZE),
            getExtrapolatorFor(OPACITY), getExtrapolatorFor(POSITION_X),
            getExtrapolatorFor(POSITION_Y), getExtrapolatorFor(ROTATION), Size(dimens.first, dimens.second))
    }

    override fun genPropertyInput(): View {
        val layout = TextInputLayout(context)
        layout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        layout.setPadding(dpToPx(6f).toInt(), 0, dpToPx(6f).toInt(), 0)
        // Add text input
        val editText = TextInputEditText(context)
        editText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        editText.hint = "Enter text here"
        editText.setText(DEFAULT_TITLE)
        layout.addView(editText)

        return layout
    }

    private fun createColorPicker(): ImageButton {
        // Add color picker input
        val imageButton = ImageButton(context)
        imageButton.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        imageButton.setPadding(dpToPx(3f).toInt())
        val circle = context.getDrawable(R.drawable.circle)
        circle?.setColorFilter(currentColor, PorterDuff.Mode.LIGHTEN)
        imageButton.setImageDrawable(circle)
        imageButton.background = context.getDrawable(android.R.color.transparent)
        imageButton.setOnClickListener {
            val dialog = ColorPickerDialog.newBuilder().setColor(currentColor).create()
            dialog.setColorPickerDialogListener(object: ColorPickerDialogListener{
                override fun onDialogDismissed(dialogId: Int) {
                }

                override fun onColorSelected(dialogId: Int, color: Int) {
                    currentColor = color
                    imageButton.setColorFilter(currentColor, PorterDuff.Mode.LIGHTEN)
                    attributeChangeListener?.onAttributeChange(this@TextEffectView)
                }
            })
            dialog.show(effectVideoBinder.myActivity.supportFragmentManager, "Hello")
        }
        return imageButton
    }

    override fun getButtonTitle(): String {
        val title = getTitle()
        if (title.isEmpty())
            return "Text Effect"
        return "Text Effect: ${getTitle()}"
    }

    override fun getKeyFrames(): TreeMap<Int, ArrayMap<String, Float>> {
        return keyFrameAttrs
    }

    private fun getTitle(): String {
        return propertyInput.text.toString()
    }

    companion object {
        const val DEFAULT_TITLE = "Enter text here"
    }
}