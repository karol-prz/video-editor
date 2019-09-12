package com.kpchuck.videoeditor

import android.os.AsyncTask
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.android.exoplayer2.upstream.cache.CacheSpan
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView
import views.VideoViewer
import java.io.File

class EffectVideoBinder(private val videoViewer: VideoViewer, val myActivity: MainActivity): VideoViewer.OnVideoChangeListener, BaseEffectView.AttributeChangeListener {

    /*
    Holds the videoUtils cursor frame
    Holds passthrough method for OnCutListener
    Holds hashmap of effect object frames to bitmap of current cursor
    Uses videoUtils viewer FrameChangeListener {
        Queries new Bitmaps from each effect
    }
    Needs an attributeChangeListener for each effect {
        Refreshes that effects bitmap
        Runs getFrame for current frame again
    }
     */

    val cursor: Int get() {
        return videoViewer.cursor
    }

    val dimens: Pair<Int, Int> get() {
        return videoViewer.dimens
    }

    val duration: Int get() { return videoViewer.duration }

    init {
        videoViewer.addVideoChangeListener(this)
    }

    fun runFilePicker(pickFile: (activity: MainActivity) -> Unit, onReturn: (file: File) -> Unit){
        pickFile(myActivity)
        myActivity.onReturnListener = onReturn
    }

    fun addNewView(view: BaseEffectView){
        videoViewer.frameMap[view] = view.getFrame(videoViewer.cursor)
        view.attributeChangeListener = this
        view.setEnd(videoViewer.duration)
    }

    fun removeView(view: BaseEffectView){
        videoViewer.frameMap.remove(view)
    }

    override fun onAttributeChange(view: BaseEffectView) {
        videoViewer.frameMap[view] = view.getFrame(videoViewer.cursor)
        videoViewer.updateEffectImages()
    }

    override fun onFrameUpdated(currentPosition: Int) {
        for (v in videoViewer.frameMap.keys){
            videoViewer.frameMap[v] = v.getFrame(currentPosition)
        }
    }

    override fun onFrameCut(newDuration: Int) {
        for (v in videoViewer.frameMap.keys)
            v.setEnd(newDuration)
    }
}