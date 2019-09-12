package com.kpchuck.videoeditor.video

import android.app.Instrumentation
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.util.Log
import android.util.Range
import android.util.SparseIntArray
import androidx.core.util.contains
import androidx.core.util.keyIterator
import androidx.core.util.remove
import com.bumptech.glide.Glide.init
import wseemann.media.FFmpegMediaMetadataRetriever
import java.sql.Time
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.concurrent.thread
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class VideoUtils(file: String) {

    private val mmr = FFmpegMediaMetadataRetriever()
   // private val mediaMediaMetadataRetriever = MediaMetadataRetriever()
    val numFrames: Int
    private val frames: LinkedHashMap<Int, Bitmap>
    private val cutRanges = SparseIntArray()

    var fps = 0f
    var numCutFrames = 0
    private var getFrame = 0
    private val scaledHeight: Pair<Int, Int>

    val dimens: Pair<Int, Int>
    val bmConfig: Bitmap.Config

    init {
        mmr.setDataSource(file)
        val duration = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION).toInt() / 1000
        val temp = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE)
        fps = temp.toFloat()
        numFrames = (duration * fps).toInt()

        scaledHeight = getScaledSize(600)

        frames = object : LinkedHashMap<Int, Bitmap>(){
            override fun removeEldestEntry(eldest: Map.Entry<Int, Bitmap>): Boolean {
                return size > (fps * 30)
            }
        }

        bmConfig = mmr.getFrameAtTime(0).config

        dimens = Pair(mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt() ,
            mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt())
    }


    fun cut(startFrame: Int, endFrame: Int){
        cutRanges.append(startFrame, endFrame-startFrame)
        numCutFrames += endFrame-startFrame
    }

    fun getNextFrame(currentFrame: Int): Int {
        var f = currentFrame
        f++
        if (f > numFrames - numCutFrames)
            f--
        return f
    }

    fun getPrevFrame(currentFrame: Int): Int {
        var f = currentFrame
        f--
        if (f < 1)
            f++
        return f
    }

    fun calcCutRanges(frame: Int): Int{
        return frame
        var f = frame
        for (r in cutRanges.keyIterator()){
            if (f >= r){
                f += cutRanges[r]
            }
        }
        return f
    }

    fun isInCutRanges(frame: Int): Boolean {
        for (r in cutRanges.keyIterator()){
            if (r <= frame)
                return true
        }
        return false
    }

    fun getFrame(frame: Int): Bitmap? {
        getFrame = 0
        getFrame += frame
        val inrange = calcCutRanges(getFrame)
        if (!frames.containsKey(inrange))
            loadFrame(inrange)
        return frames[inrange]
    }

    private fun loadFrame(position: Int){
        if (frames.containsKey(position)) return
        Log.d("kpchuck", "Loading frame $position")
       // val newFrame = mediaMediaMetadataRetriever.getFrameAtIndex(position)
       // val newFrame = mmr.getScaledFrameAtTime(getTime(position), FFmpegMediaMetadataRetriever.OPTION_CLOSEST, scaledHeight.first, scaledHeight.second)
       // frames[position] = newFrame
    }

//    private fun getFullResFrame(position: Int): Bitmap?{
//     //   return mmr.getFrameAtTime(getTime(position), FFmpegMediaMetadataRetriever.OPTION_CLOSEST)
//    }

    fun getTime(frame: Int): Long {
        return (frame * fps).roundToLong()
//        return ((1000000/fps) * frame).roundToLong()
    }

    fun getFrameFromTime(time: Long): Int {
        return (time/fps).roundToInt()
    }

    private fun getScaledSize(maxSize: Int): Pair<Int, Int>{
//        var width = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
//        var height = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
//
//        val bitmapRatio = width.toFloat() / height.toFloat()
//        if (bitmapRatio > 1) {
//            width = maxSize
//            height = (width / bitmapRatio).toInt()
//        } else {
//            height = maxSize
//            width = (height * bitmapRatio).toInt()
//        }
//        return Pair(width, height)
        return Pair(1080, 1800)
    }

}