package com.kpchuck.videoeditor.controllers

import android.util.Log
import android.widget.*
import com.kpchuck.videoeditor.R
import androidx.cardview.widget.CardView
import com.kpchuck.videoeditor.MainActivity
import com.kpchuck.videoeditor.utils.LongPressHandler
import com.kpchuck.videoeditor.utils.TimeUtils
import com.kpchuck.videoeditor.views.rangebars.CustomSeekBarView
import views.VideoViewer
import kotlin.concurrent.thread


class SeekBarController(val seekBarView: CardView, val videoViewer: VideoViewer, val mainActivity: MainActivity):
    SeekBar.OnSeekBarChangeListener, VideoViewer.OnVideoChangeListener {

    private val seekBar = seekBarView.findViewById<CustomSeekBarView>(R.id.seekBarVideoPosition)
    private val prevFrame = seekBarView.findViewById<ImageButton>(R.id.prevFrameButton)
    private val nextFrame = seekBarView.findViewById<ImageButton>(R.id.nextFrameButton)
    private val playButton = seekBarView.findViewById<ImageButton>(R.id.playButton)
    private val startCut = seekBarView.findViewById<Button>(R.id.startCutButton)
    private val endCut = seekBarView.findViewById<Button>(R.id.endCutButton)
    private val currentTime = seekBarView.findViewById<TextView>(R.id.currentTime)
    private val endTime = seekBarView.findViewById<TextView>(R.id.endTime)

    private var firstTime = true
    private var seeking = false


    init {
        seekBar.max = videoViewer.duration
        seekBar.setOnSeekBarChangeListener(this)
        videoViewer.addVideoChangeListener(this)

        prevFrame.setOnClickListener { prevFrame() }
        nextFrame.setOnClickListener { nextFrame() }
        LongPressHandler(prevFrame, 50) { prevFrame() }
        LongPressHandler(nextFrame, 50) { nextFrame() }
        playButton.setOnClickListener { play() }

        startCut.setOnClickListener { startCutting() }
    }

    private fun startCutting(){
        seekBar.freezeSecondary()
        endCut.setOnClickListener { endCutting() }
    }

    private fun endCutting(){
        endCut.setOnClickListener {  }
        videoViewer.cutVideo(seekBar.secondaryPosition, seekBar.progress)
        seekBar.resetSecondary()
    }

    private fun nextFrame(){
        if (seeking) return
        videoViewer.nextFrame()
        seeking = true
    }

    private fun prevFrame(){
        if (seeking) return
        videoViewer.prevFrame()
        seeking = true
    }

    private fun setSeekBarToCursor(){
        seekBar.progress = videoViewer.seekPosition
    }

    private fun play(){
        playButton.setImageResource(android.R.drawable.ic_media_pause)
        playButton.setOnClickListener { pause() }
        videoViewer.startPlaying()
    }

    private fun pause(){
        videoViewer.stopPlaying()
        playButton.setImageResource(android.R.drawable.ic_media_play)
        playButton.setOnClickListener { play() }

    }

    override fun onProgressChanged(p0: SeekBar?, position: Int, fromUser: Boolean) {
        if (fromUser)
            videoViewer.seekTo(position)
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
        videoViewer.setFasterSeek()
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        videoViewer.setAccurateSeek()
    }

    override fun onFrameUpdated(currentPosition: Int) {
        currentTime.text = TimeUtils.formatTime(currentPosition.toLong())
        seekBar.progress = currentPosition
        if (firstTime) {
            seekBar.max = videoViewer.duration
            endTime.text = TimeUtils.formatTime(videoViewer.duration.toLong())
            firstTime = false
        }
        seeking = false
    }

    override fun onFrameCut(newDuration: Int) {
        seekBar.max = videoViewer.duration
        endTime.text = TimeUtils.formatTime(videoViewer.duration.toLong())
    }
}