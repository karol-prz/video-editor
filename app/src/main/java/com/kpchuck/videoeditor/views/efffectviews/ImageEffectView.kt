package com.kpchuck.videoeditor.views.efffectviews

import android.content.Context
import android.graphics.*
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.view.get
import com.bumptech.glide.Glide.init
import com.kpchuck.videoeditor.EffectVideoBinder
import com.kpchuck.videoeditor.R
import com.kpchuck.videoeditor.controllers.AttributeViewController
import com.kpchuck.videoeditor.controllers.PropertySeekBarController
import droidninja.filepicker.FilePickerBuilder
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.util.Size
import com.kpchuck.Drawer
import com.kpchuck.Extrapolator
import com.kpchuck.ImageDrawer
import kotlin.math.roundToInt
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView.Companion.SIZE_PERCENT
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView.Companion.OPACITY
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView.Companion.POSITION_X
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView.Companion.POSITION_Y
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView.Companion.ROTATION


class ImageEffectView (override val context: Context, val effectVideoBinder: EffectVideoBinder): BaseEffectView {


    override val rootView: CardView = LayoutInflater.from(context).inflate(R.layout.property_view, null) as CardView
    override val attributeController: AttributeViewController
    override val propertySeekBarController: PropertySeekBarController
    override var attributeChangeListener: BaseEffectView.AttributeChangeListener? = null
    private val propertyInput: Button
    override val keyFrameAttrs = TreeMap<Int, ArrayMap<String, Float>>()
    override val defaultAttrs: HashMap<String, Int> = hashMapOf(
        Pair(BaseEffectView.SIZE_PERCENT, 50),
        Pair(BaseEffectView.POSITION_X, 0),
        Pair(BaseEffectView.POSITION_Y, 0),
        Pair(BaseEffectView.OPACITY, 100),
        Pair(BaseEffectView.ROTATION, 0)
    )
    override var extrapolatorKeys = ArrayMap<String, ArrayMap<Int, Float>>()
    private var currentFile: File? = null
    private var currentBitmap: Bitmap? = null

    init {
        val inputLayout = genPropertyInput() as LinearLayout
        val wrapper = rootView.findViewById<LinearLayout>(R.id.effectSelectorWrapper)
        wrapper.addView(inputLayout)
        propertyInput = inputLayout[0] as Button

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
        if (position < duration.first || position > duration.second || currentBitmap == null)
            return null
        val dimens = effectVideoBinder.dimens
        val dest = Bitmap.createBitmap(dimens.first, dimens.second, Bitmap.Config.ARGB_8888)
        val cs = Canvas(dest)
        val imageDrawer = getDrawer()
        imageDrawer.draw(cs, position)
        return dest
    }

    override fun getDrawer(): Drawer {
        val dimens = effectVideoBinder.dimens
        genExtrapolatorKeys()
        return ImageDrawer(getExtrapolatorFor(SIZE_PERCENT), getExtrapolatorFor(OPACITY),
            getExtrapolatorFor(POSITION_X), getExtrapolatorFor(POSITION_Y), getExtrapolatorFor(ROTATION),
            currentBitmap!!, Size(dimens.first, dimens.second)
        )
    }

    override fun genPropertyInput(): View {
        val layout = LinearLayout(context)
        layout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        layout.setPadding(dpToPx(6f).toInt(), 0, dpToPx(6f).toInt(), 0)
        // Add text input
        val inputButton = Button(context)
        inputButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        inputButton.text = PICK_IMAGE
        inputButton.setOnClickListener { openImagePicker() }
        layout.addView(inputButton)

        return layout
    }

    private fun openImagePicker(){
        val filePicker = FilePickerBuilder.instance.setMaxCount(1)
            .enableImagePicker(true)
            .setActivityTheme(R.style.LibAppTheme)
        effectVideoBinder.runFilePicker(filePicker::pickPhoto) {
            currentFile = it
            propertyInput.text = getTitle()
            val bmOptions = BitmapFactory.Options()
            currentBitmap = BitmapFactory.decodeFile(currentFile?.absolutePath, bmOptions)
            attributeChangeListener?.onAttributeChange(this)
        }
    }

    override fun getButtonTitle(): String {
        if (currentFile == null)
            return "Image Effect"
        return "Image Effect: ${getTitle()}"
    }

    override fun getKeyFrames(): TreeMap<Int, ArrayMap<String, Float>> {
        return keyFrameAttrs
    }

    private fun getTitle(): String {
        return currentFile?.nameWithoutExtension ?: ""
    }

    companion object {
        const val PICK_IMAGE = "Pick an Image"
    }
}