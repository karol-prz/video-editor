package com.kpchuck.videoeditor.views

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import com.kpchuck.videoeditor.video.Audio
import com.kpchuck.videoeditor.video.Video
import wseemann.media.FFmpegMediaMetadataRetriever
import kotlin.math.absoluteValue


class VideoViewer(private val imageView: ImageView, file: String) {

    var cursor = 0
    val video = Video(file)
    private val videoChangeListeners = ArrayList<OnVideoChangeListener>()

    //val audio = Audio(context, file)

    init {
        updateImage()
    }

    val numFrames: Int
        get(){
            return video.numFrames - video.numCutFrames
        }

    fun addVideoChangeListener(listener: OnVideoChangeListener){
        videoChangeListeners.add(listener)
    }

    fun nextFrame(){
        cursor = video.getNextFrame(cursor)
        updateImage()
    }

    fun prevFrame(){
        cursor = video.getPrevFrame(cursor)
        updateImage()
    }

    fun setFrame(newPosition: Int){
        cursor = newPosition
        updateImage()
        for (i in videoChangeListeners)
            i.onFrameChanged(newPosition)
    }

    fun cutVideo(startFrame: Int, endFrame: Int){
        var start = startFrame
        var end = endFrame
        if (startFrame > endFrame) {
            start = endFrame
            end = startFrame
        }
        video.cut(start, end)
        for (i in videoChangeListeners){
            i.onFramesCut(start, end)
        }
        cursor = startFrame + 1
        updateImage()
    }

    private fun updateImage(){
        imageView.setImageBitmap(video.getFrame(cursor))
        for (i in videoChangeListeners)
            i.onFrameChanged(cursor)
    }

    interface OnVideoChangeListener {
        fun onFrameChanged(position: Int)
        fun onFramesCut(startPosition: Int, endPosition: Int)
    }

}