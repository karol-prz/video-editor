package com.kpchuck.videoeditor.views.rangebars

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import android.widget.*
import com.kpchuck.videoeditor.R
import com.kpchuck.videoeditor.utils.LongPressHandler
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.kpchuck.videoeditor.utils.SwipeDetector
import com.kpchuck.videoeditor.utils.TimeUtils
import kotlin.math.roundToLong


class SingleRowSeekBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
    ): LinearLayout(context, attrs, defStyle), SeekBar.OnSeekBarChangeListener {

    private val textView: TextView
    private val incButton: ImageButton
    private val decButton: ImageButton
    private val lockButton: ImageButton
    private val seekBar: SeekBar

    private val lockDrawable = context.getDrawable(R.drawable.ic_baseline_lock_24px)
    private val unlockDrawable = context.getDrawable(R.drawable.ic_baseline_lock_open_24px)

    var textInfo = "Default Text"
    var seekBarListener: SeekBar.OnSeekBarChangeListener? = null
    var usingPercent = false
    private var disableButton = false

    // Attributes that need to be changed passed through
    var text: String
    get() {
        return textView.text.toString()
    }
    set(value) {
        textInfo = value
        updateTextView()
    }

    var allowDisabling: Boolean
    get() = disableButton
    set(value) {
        disableButton = value
        lockButton.visibility = if (disableButton) View.VISIBLE else View.GONE
    }

    var progress: Float
    get() {
        return if (usingPercent) seekBar.progress.toFloat() / 4 else seekBar.progress.toFloat()
    }
    set(value) {
        val currentValue = if (usingPercent) (value * 4).toInt() else value.toInt()
        seekBar.progress = currentValue
        updateTextView()
    }

    var max: Int
    get() {
        return if (usingPercent) seekBar.max / 4 else seekBar.max
    }
    set(value) {
        val currentValue = progress
        seekBar.max = if (usingPercent) value * 4 else value
        progress = if (currentValue <= seekBar.max) currentValue else seekBar.max.toFloat()
        updateTextView()
    }

    var frameTime: Int?
    get() {
        parent ?: return null
        val parentView = parent as LinearLayout
        return parentView.tag.toString().toInt()
    }
    set(value) {
        val parentView = parent as LinearLayout
        parentView.tag = value
    }

    private var mFormatToTime = false
    var formatToTime: Boolean
    get() = mFormatToTime
    set(value) {
        mFormatToTime = value
        updateTextView()
    }

    init {
        inflate(context, R.layout.single_item_range_bar, this)
        textView = findViewById(R.id.title)
        incButton = findViewById(R.id.addButton)
        decButton = findViewById(R.id.substractButton)
        lockButton = findViewById(R.id.lockButton)
        seekBar = findViewById(R.id.rowSeekBar)
        textView.setOnClickListener { openInputDialog() }
        incButton.setOnClickListener { incSeekBar() }
        decButton.setOnClickListener { decSeekBar() }
        LongPressHandler(incButton) {incSeekBar()}
        LongPressHandler(decButton) {decSeekBar()}

        seekBar.setOnSeekBarChangeListener(this)
        seekBar.setOnTouchListener(SwipeDetector())

        lockButton.setOnClickListener { disableLayout() }

        val a = context.obtainStyledAttributes(attrs, R.styleable.SingleRowSeekBar, defStyle, 0)
        text = a.getString(R.styleable.SingleRowSeekBar_title) ?: ""
        allowDisabling = a.getBoolean(R.styleable.SingleRowSeekBar_allowDisabling, false)
        formatToTime = a.getBoolean(R.styleable.SingleRowSeekBar_showTime, false)
        a.recycle()
    }

    fun setOnSeekBarChangeListener(listener: SeekBar.OnSeekBarChangeListener){
        seekBarListener = listener
    }

    private fun incSeekBar(){
        if (seekBar.progress + 1 > seekBar.max)
            return
        progress += if (usingPercent) 0.25f else 1f
        seekBarListener?.onProgressChanged(seekBar, progress.toInt(), true)
    }

    private fun decSeekBar(){
        if (seekBar.progress - 1 < 0)
            return
        progress -= if (usingPercent) 0.25f else 1f
        seekBarListener?.onProgressChanged(seekBar, progress.toInt(), true)
    }

    private fun openInputDialog(){
        val dialogBuilder = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
        val inputLayout = LayoutInflater.from(context).inflate(R.layout.input_dialog_layout, null)
        val inputView = inputLayout.findViewById<TextInputEditText>(R.id.textInputDialog)
        inputView.inputType = if (usingPercent) EditorInfo.TYPE_NUMBER_FLAG_DECIMAL else EditorInfo.TYPE_CLASS_NUMBER
        dialogBuilder.setView(inputLayout)
        dialogBuilder.setPositiveButton("Ok") { dialog, id ->
            var num = 0f
            if (inputView.text!!.isNotEmpty()){
                num = inputView.text.toString().toFloat()
            }
            if (num > max) max = num.toInt()
            progress = num
        }
        dialogBuilder.create().show()
    }

    private fun enableLayout(){
        for (v in arrayOf(incButton, decButton, seekBar, textView)) {
            v.isClickable = true
            v.isEnabled = true
        }
        lockButton.setImageDrawable(unlockDrawable)
        lockButton.setOnClickListener { disableLayout() }
        seekBarListener?.onProgressChanged(seekBar, progress.toInt(), true)
    }

    private fun disableLayout(){
        for (v in arrayOf(incButton, decButton, seekBar, textView)) {
            v.isClickable = false
            v.isEnabled = false
        }
        lockButton.setImageDrawable(lockDrawable)
        lockButton.setOnClickListener { enableLayout() }
        seekBarListener?.onProgressChanged(seekBar, -1, true)
    }

    private fun updateTextView(){
        val currentValue = if (usingPercent) seekBar.progress.toFloat() / 4 else seekBar.progress.toFloat()
        val textValue = if (formatToTime) TimeUtils.formatTime(currentValue.roundToLong()) else currentValue.toString()
        textView.text = String.format("%s: %s", textInfo, textValue)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        updateTextView()
        seekBarListener?.onProgressChanged(seekBar, this.progress.toInt(), fromUser)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        seekBarListener?.onStartTrackingTouch(seekBar)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        seekBarListener?.onStopTrackingTouch(seekBar)
    }

}