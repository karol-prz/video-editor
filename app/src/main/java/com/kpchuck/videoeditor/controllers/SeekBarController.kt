package com.kpchuck.videoeditor.controllers

import android.util.Log
import android.widget.*
import com.kpchuck.videoeditor.R
import androidx.cardview.widget.CardView
import com.appyvet.materialrangebar.RangeBar
import com.kpchuck.videoeditor.MainActivity
import com.kpchuck.videoeditor.utils.LongPressHandler
import com.kpchuck.videoeditor.views.VideoViewer
import com.kpchuck.videoeditor.views.rangebars.CustomSeekBarView


class SeekBarController(val seekBarView: CardView, val videoViewer: VideoViewer, val mainActivity: MainActivity):
    RangeBar.OnRangeBarChangeListener, VideoViewer.OnVideoChangeListener {

    private val seekBar = seekBarView.findViewById<CustomSeekBarView>(R.id.seekBar)
    private val prevFrame = seekBarView.findViewById<ImageButton>(R.id.prevFrameButton)
    private val nextFrame = seekBarView.findViewById<ImageButton>(R.id.nextFrameButton)
    private val playButton = seekBarView.findViewById<ImageButton>(R.id.playButton)
    private val startCut = seekBarView.findViewById<Button>(R.id.startCutButton)
    private val endCut = seekBarView.findViewById<Button>(R.id.endCutButton)

    private var playing = false

    init {
        seekBar.tickEnd = videoViewer.video.numFrames.toFloat()
        seekBar.setOnRangeBarChangeListener(this)
        videoViewer.addVideoChangeListener(this)

        prevFrame.setOnClickListener { prevFrame() }
        nextFrame.setOnClickListener { nextFrame() }
        LongPressHandler(prevFrame, 100) { prevFrame() }
        LongPressHandler(nextFrame, 100) { nextFrame() }
        playButton.setOnClickListener { play() }

        startCut.setOnClickListener { startCutting() }
        videoViewer.setFrame(1)
    }

    private fun startCutting(){
        seekBar.freezeSecondary()
        endCut.setOnClickListener { endCutting() }
    }

    private fun endCutting(){
        endCut.setOnClickListener {  }
        videoViewer.cutVideo(seekBar.secondaryPosition, seekBar.rightIndex)
        setSeekBarToCursor()
        seekBar.tickEnd = (videoViewer.video.numFrames - videoViewer.video.numCutFrames).toFloat()
        seekBar.resetSecondary()
    }

    private fun nextFrame(){
        videoViewer.nextFrame()
    }

    private fun prevFrame(){
        videoViewer.prevFrame()
    }

    private fun setSeekBarToCursor(){
        mainActivity.runOnUiThread { seekBar.setSeekPinByValue(videoViewer.cursor.toFloat()) }
    }

    private fun play(){
        playButton.setImageResource(android.R.drawable.ic_media_pause)
        playButton.setOnClickListener { pause() }
        playing = true
        Thread(Runnable {
            while (playing){
                try {
                    val startTime = System.nanoTime()
                    videoViewer.nextFrame()
                    setSeekBarToCursor()
                    // Time to wait between frames minus time took to execute code
                    val playTime = ((1000 / videoViewer.video.fps).toLong()) - (System.nanoTime() - startTime)/1000000
                    if (playTime > 0)
                        Thread.sleep(playTime)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }).start()
    }

    private fun pause(){
        playing = false
        playButton.setImageResource(android.R.drawable.ic_media_play)
        playButton.setOnClickListener { play() }

    }

    override fun onTouchEnded(rangeBar: RangeBar?) {
    }

    override fun onRangeChangeListener(rangeBar: RangeBar?, leftPinIndex: Int, rightPinIndex: Int, leftPinValue: String?, rightPinValue: String?) {
        Log.d("kpchuck", "Displaying frame $rightPinValue")
        videoViewer.setFrame(rightPinValue!!.toInt())
    }

    override fun onTouchStarted(rangeBar: RangeBar?) {
    }

    override fun onFrameChanged(position: Int) {
        if (seekBar.rightIndex != position && !playing)
            setSeekBarToCursor()
    }

    override fun onFramesCut(startPosition: Int, endPosition: Int) {
    }
}