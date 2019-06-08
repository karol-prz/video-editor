package com.kpchuck.videoeditor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.cardview.widget.CardView
import com.kpchuck.videoeditor.controllers.AttributeViewController
import com.kpchuck.videoeditor.controllers.EffectViewController
import com.kpchuck.videoeditor.utils.SwipeDetector
import com.kpchuck.videoeditor.controllers.RangeBarController
import com.kpchuck.videoeditor.controllers.SeekBarController
import com.kpchuck.videoeditor.views.VideoViewer


class MainActivity : AppCompatActivity() {

    lateinit var effectViewController: EffectViewController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val sbView = findViewById<CardView>(R.id.seekbarWrapper)
        val videoViewer = VideoViewer(imageView, Environment.getExternalStorageDirectory().absolutePath + "/hellothere.mp4")
        val seekBarView = SeekBarController(sbView, videoViewer, this)

        val effectView = findViewById<LinearLayout>(R.id.selectPropertyView)
        effectViewController = EffectViewController(effectView, this, videoViewer, this)

    }

    override fun onBackPressed() {
        if (effectViewController.hasPropertyOpen()) {
            effectViewController.closeProperty()
            return
        }
        super.onBackPressed()
    }
}
