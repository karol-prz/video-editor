package com.kpchuck.videoeditor.controllers

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.view.iterator
import com.kpchuck.videoeditor.MainActivity
import com.kpchuck.videoeditor.R
import com.kpchuck.videoeditor.views.VideoViewer
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView
import com.kpchuck.videoeditor.views.efffectviews.TextEffectView
import android.os.Handler
import androidx.core.os.postAtTime
import androidx.core.view.get
import androidx.core.view.size


class EffectViewController(private val effectView: LinearLayout, private val context: Context, private val videoViewer: VideoViewer,
                           private val mainActivity: MainActivity): VideoViewer.OnVideoChangeListener {

    private val addedViews = ArrayList<BaseEffectView>()
    private val addViewButton = effectView.findViewById<Button>(R.id.addEffectButton)

    init {
        addViewButton.setOnClickListener { addTextView() }
        videoViewer.addVideoChangeListener(this)
    }

    fun hasPropertyOpen(): Boolean{
        return effectView.size == 2
    }

    fun closeProperty(){
        removeView(addedViews[effectView[0].tag.toString().toInt()].rootView)
    }

    private fun addTextView(){
        val textView = TextEffectView(context)
        addedViews.add(textView)
        refreshViews()
        openView(textView)
    }

    fun refreshViews(){
        effectView.removeAllViews()
        for ((index, view) in addedViews.withIndex()){
            val button = Button(context)
            button.text = view.getButtonTitle()
            button.tag = index
            button.setOnClickListener { openView(view) }
            effectView.addView(button)
        }
        effectView.addView(addViewButton)
    }

    private fun openView(view: BaseEffectView){
        val rootView = view.rootView
        (effectView.parent as ViewGroup).addView(rootView, 0)
        val animSlide = AnimationUtils.loadAnimation(context, R.anim.slide_in)
        rootView.startAnimation(animSlide)
        view.setEnd(videoViewer.numFrames)
        view.setShowing{removeView(rootView)}
    }

    private fun removeView(rootView: CardView){
        val animSlideOut = AnimationUtils.loadAnimation(context, R.anim.slide_out)
        animSlideOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                rootView.visibility = View.GONE
                val h = Handler()
                h.postAtTime({
                        mainActivity.runOnUiThread {
                            try {
                                while (rootView.parent != null) {
                                    (rootView.parent as FrameLayout).removeView(rootView)
                                    refreshViews()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }, 10
                )
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        rootView.startAnimation(animSlideOut)
    }

    override fun onFrameChanged(position: Int) {

    }

    override fun onFramesCut(startPosition: Int, endPosition: Int) {
        for (view in addedViews)
            view.setEnd(videoViewer.numFrames)
    }
}