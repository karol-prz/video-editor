package com.kpchuck.videoeditor.controllers

import android.content.Context
import android.util.ArrayMap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.kpchuck.videoeditor.R
import me.relex.circleindicator.CircleIndicator2
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.kpchuck.videoeditor.EffectVideoBinder
import com.kpchuck.videoeditor.utils.OnSwipeListener
import com.kpchuck.videoeditor.views.rangebars.SingleRowSeekBar
import java.util.*
import kotlin.collections.ArrayList


class AttributeViewController(attributeView: CardView, context: Context, val keyFrameAttrs: TreeMap<Int, ArrayMap<String, Float>>,
                              val orderedAttrs: List<String>, val effectVideoBinder: EffectVideoBinder, val updateKeyFrames: () -> Unit){

    private val recyclerView = attributeView.findViewById<RecyclerView>(R.id.attributeRecyclerView)
    private val circleIndicator = attributeView.findViewById<CircleIndicator2>(R.id.attributeIndicator)
    val layoutManager: CustomLinearLayoutManager

    val gestureDector = GestureDetectorCompat(context, object : OnSwipeListener(){
        override fun onSwipe(direction: Direction): Boolean {
            if (direction == Direction.left)
                scrollBy(1)
            else if (direction == Direction.right)
                scrollBy(-1)
            return super.onSwipe(direction)
        }
    })

    init {
        val adapter = KeyFramePagerAdapter()
        layoutManager = CustomLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        //recyclerView.addOnItemTouchListener(this)

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(recyclerView)

        circleIndicator.attachToRecyclerView(recyclerView, pagerSnapHelper)
        circleIndicator.setOnTouchListener { view, motionEvent -> return@setOnTouchListener gestureDector.onTouchEvent(motionEvent) }
        // optional
        adapter.registerAdapterDataObserver(circleIndicator.adapterDataObserver)
    }

    private fun scrollBy(amount: Int){
        val currentPosition = layoutManager.findFirstVisibleItemPosition()
        Log.d("kpchuck", "Current position is $currentPosition")
        layoutManager.scrollToPosition(currentPosition + amount)
    }

    inner class KeyFramePagerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(), SeekBar.OnSeekBarChangeListener{


        inner class FrameViewHolder(view: LinearLayout, val seekBar: SingleRowSeekBar): RecyclerView.ViewHolder(view)

        inner class LastViewHolder(view: Button): RecyclerView.ViewHolder(view)

        override fun getItemViewType(position: Int): Int {
            if (position + 1 == itemCount)
                return TypeEnd
            return TypeKeyFrame
        }

        override fun getItemCount(): Int {
            return keyFrameAttrs.size + 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val holder: RecyclerView.ViewHolder?
            if (viewType == TypeEnd){
                val button = Button(parent.context)
                button.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                button.text = "New KeyFrame"
                holder = LastViewHolder(button)
            } else {
                val layout = LinearLayout(parent.context)
                layout.orientation = LinearLayout.VERTICAL
                layout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                for (attr in orderedAttrs){
                    layout.addView(setupSeekBar(attr, parent.context))
                }
                val sbLayout = createKeyFrameSeekBar(parent.context)
                layout.addView(sbLayout, 0)
                holder = FrameViewHolder(layout, sbLayout)
            }
            return holder
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder.itemViewType == TypeEnd){
                val h = holder as LastViewHolder
                h.itemView.setOnClickListener { addNewKeyFrame() }
            } else {
                val frame = getFrameFromPosition(position)
                (holder.itemView as LinearLayout).tag = frame
                for (singleRowSeekBar in (holder.itemView as LinearLayout).children){
                    if (singleRowSeekBar !is SingleRowSeekBar)
                        continue
                    // Set the value of the seekbar to the default value
                    if (keyFrameAttrs[frame]!!.contains(singleRowSeekBar.textInfo))
                        singleRowSeekBar.progress = keyFrameAttrs[frame]!![singleRowSeekBar.textInfo]!!
                    else
                        singleRowSeekBar.progress = frame.toFloat()
                }
                // If key frame: set key frame range bar to the frame
                val h = holder as FrameViewHolder
                h.seekBar.progress = frame.toFloat()
            }
        }

        private fun addNewKeyFrame(){
            var keyFramePosition = effectVideoBinder.cursor
            while (keyFrameAttrs.contains(keyFramePosition)) keyFramePosition++
            val lastPosition = getFrameFromPosition(keyFrameAttrs.size-1)
            val arrayMap = ArrayMap<String, Float>()
            for ((k, v) in keyFrameAttrs[lastPosition]!!.entries){
                arrayMap[k] = v
            }
            keyFrameAttrs[keyFramePosition] = arrayMap
            notifyItemRangeChanged(0, keyFrameAttrs.size + 1)
            layoutManager.scrollToPosition(getPositionFromFrame(keyFramePosition))
            updateKeyFrames()
        }

        private fun createKeyFrameSeekBar(context: Context): SingleRowSeekBar {
            val seekBar = setupSeekBar("KeyFrame Time", context)
            seekBar.max = effectVideoBinder.duration
            seekBar.allowDisabling = false
            seekBar.formatToTime = true
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekbar: SeekBar?, p1: Int, p2: Boolean) {
                    updateKeyFrame(seekbar!!)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    layoutManager.setScrollEnabled(false)
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    layoutManager.setScrollEnabled(true)
                    notifyDataSetChanged()
                    layoutManager.scrollToPosition(getPositionFromFrame(seekBar!!.progress))
                }
            })
            return seekBar
        }

        private fun setupSeekBar(attr: String, context: Context): SingleRowSeekBar {
            val seekBar = SingleRowSeekBar(context)
            seekBar.text = attr
            seekBar.setOnSeekBarChangeListener(this)
            if (attr.contains("Percent"))
                seekBar.usingPercent = true
            seekBar.max = 100
            seekBar.allowDisabling = true
            return seekBar
        }

        private fun updateFrameAttrs(rangeBar: SeekBar, progress: Float){
            val seekBarRow = (rangeBar.parent.parent.parent as SingleRowSeekBar)
            val currentPosition = seekBarRow.frameTime ?: return
            val key = seekBarRow.textInfo
            keyFrameAttrs[currentPosition]!![key] = if (progress == -1f) progress else seekBarRow.progress
            updateKeyFrames()
        }

        private fun updateKeyFrame(rangeBar: SeekBar){
            val seekBarRow = (rangeBar.parent.parent.parent as SingleRowSeekBar)
            val value = seekBarRow.progress.toInt()
            val currentPosition = seekBarRow.frameTime ?: return
            val tempAttrs = TreeMap<Int, ArrayMap<String, Float>>()
            for (i in keyFrameAttrs.keys){
                if (i == currentPosition.toString().toInt()){
                    tempAttrs[value] = keyFrameAttrs[i]!!
                } else
                    tempAttrs[i] = keyFrameAttrs[i]!!
            }
            keyFrameAttrs.clear()
            keyFrameAttrs.putAll(tempAttrs)
            seekBarRow.frameTime = value
            updateKeyFrames()
        }

        private fun getFrameFromPosition(position: Int): Int {
            val keySet = keyFrameAttrs.navigableKeySet()
            return keySet.elementAt(position)
        }

        private fun getPositionFromFrame(frame: Int): Int {
            val keySet = keyFrameAttrs.navigableKeySet()
            return keySet.indexOf(frame)
        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
            updateFrameAttrs(seekBar!!, progress.toFloat())
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
            layoutManager.setScrollEnabled(false)
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
            layoutManager.setScrollEnabled(true)
        }
    }

    inner class CustomLinearLayoutManager(context: Context, orientation: Int, reverseLayout: Boolean) : LinearLayoutManager(context, orientation, reverseLayout){
        private var isScrollEnabled = true

        fun setScrollEnabled(flag: Boolean) {
            this.isScrollEnabled = flag
        }

        override fun canScrollHorizontally(): Boolean {
            //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
            return isScrollEnabled && super.canScrollHorizontally()
        }
    }

    companion object {
        val TypeKeyFrame = 1
        val TypeEnd = 2
    }
}