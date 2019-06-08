package com.kpchuck.videoeditor.video

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.kpchuck.videoeditor.extractor.AudioExtractor
import android.R.attr.start
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log


class Audio(context: Context, src: String) {

    private val mediaPlayer = MediaPlayer()


    init {
        mediaPlayer.setDataSource(src)
        mediaPlayer.prepare()
    }

    fun playSlice(from: Long, to: Long){
        Log.d("kpchuck", mediaPlayer.currentPosition.toString())
        mediaPlayer.seekTo(from, MediaPlayer.SEEK_CLOSEST)
        Log.d("kpchuck", mediaPlayer.currentPosition.toString())
        mediaPlayer.start()
        Log.d("kpchuck", mediaPlayer.currentPosition.toString())

        Thread(Runnable {
            Looper.prepare()
            mediaPlayer.seekTo(from, MediaPlayer.SEEK_CLOSEST)
            mediaPlayer.start()
            val timer = object : CountDownTimer(to - from, to - from) {

                override fun onTick(millisUntilFinished: Long) {
                    // Nothing to do
                }
                override fun onFinish() {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.stop()
                    }
                }
            }
            timer.start()
        })
    }
}