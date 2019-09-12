package com.kpchuck.videoeditor.controllers

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.kpchuck.videoeditor.R
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView
import com.kpchuck.videoeditor.views.efffectviews.TextEffectView
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.core.view.size
import com.kpchuck.videoeditor.EffectVideoBinder
import com.kpchuck.videoeditor.views.efffectviews.ImageEffectView
import java.util.*
import kotlin.collections.ArrayList


class EffectViewController(private val propertyFrame: FrameLayout, private val context: Context,
                           private val effectVideoBinder: EffectVideoBinder, private val activity: Activity){

    private val addedViews = ArrayList<BaseEffectView>()
    private val effectView = propertyFrame.findViewById<LinearLayout>(R.id.selectPropertyView)
    private val addViewButton = propertyFrame.findViewById<Button>(R.id.addEffectButton)
    private val effectsMap = hashMapOf<String, () -> BaseEffectView>(
        Pair("Text", { TextEffectView(context, effectVideoBinder) }),
        Pair("Image", { ImageEffectView(context, effectVideoBinder)})
    )


    init {
        addViewButton.setOnClickListener { showEffectDialog() }
    }

    private fun showEffectDialog(){
        // setup the alert builder
        val builder = AlertDialog.Builder(context, R.style.MaterialBaseTheme_Dialog)
        builder.setTitle("Choose an Effect")

        // add a radio button list
        val effectsList = effectsMap.keys.sorted().toTypedArray()
        var checkedItem = 0
        builder.setSingleChoiceItems(effectsList, checkedItem) { dialog, which ->
            // User selected an item
            checkedItem = which
        }

        // add OK and Cancel buttons
        builder.setPositiveButton("OK") { dialog, which ->
            Log.d("kpchuck", "Which is $checkedItem")
            val view = effectsMap[effectsList[checkedItem]]?.invoke() ?: return@setPositiveButton
            addedViews.add(view)
            effectVideoBinder.addNewView(view)
            refreshViews()
            openView(view)
        }
        builder.setNegativeButton("Cancel", null)

        // create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
    }

    fun hasPropertyOpen(): Boolean{
        return propertyFrame.size > 1
    }

    fun closeProperty(){
        val openedView = propertyFrame[propertyFrame.size-1] as CardView
        removeView(openedView)
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
        propertyFrame.addView(rootView)
        val animSlide = AnimationUtils.loadAnimation(context, R.anim.slide_in)
        rootView.startAnimation(animSlide)
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
                        activity.runOnUiThread {
                            try {
                                while (rootView.parent != null) {
                                    (rootView.parent as FrameLayout).removeView(rootView)
                                    refreshViews()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }, 5
                )
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        rootView.startAnimation(animSlideOut)

    }
}