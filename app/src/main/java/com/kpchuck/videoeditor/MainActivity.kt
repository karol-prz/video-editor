package com.kpchuck.videoeditor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.children
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.composer.Mp4Composer
import com.daasuu.mp4compose.dynamicfilter.GlDrawerFilter
import com.daasuu.mp4compose.filter.GlFilter
import com.daasuu.mp4compose.filter.GlFilterGroup
import com.google.android.material.snackbar.Snackbar
import com.kpchuck.videoeditor.controllers.EffectViewController
import com.kpchuck.videoeditor.controllers.SeekBarController
import com.kpchuck.videoeditor.video.VideoEditor
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import views.VideoViewer
import java.io.File
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.TextView
import com.kpchuck.videoeditor.activities.SplashActivity
import io.reactivex.internal.util.HalfSerializer.onComplete
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(){

    lateinit var effectViewController: EffectViewController
    var onReturnListener: ((file: File) -> Unit)? = null
    lateinit var videoViewer: VideoViewer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fileName = intent.getStringExtra(SplashActivity.FILENAME)
        videoViewer = VideoViewer(videoViewLayout, fileName, this)
        val seekBarView = SeekBarController(seekbarWrapper, videoViewer, this)
        val effectVideoBinder = EffectVideoBinder(videoViewer, this)
        effectViewController = EffectViewController(propertyViewFrame, this, effectVideoBinder, this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.render -> startRenderVideo()
        }
        return true
    }

    override fun onBackPressed() {
        if (::effectViewController.isInitialized.not()){
            super.onBackPressed()
            return
        }
        if (effectViewController.hasPropertyOpen()) {
            effectViewController.closeProperty()
            return
        }
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (onReturnListener != null && resultCode == Activity.RESULT_OK && data != null){
            val files = ArrayList<String>()
            when (requestCode) {
                FilePickerConst.REQUEST_CODE_PHOTO ->  {
                    files.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))
                }
                FilePickerConst.REQUEST_CODE_DOC ->  {
                    files.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS))
                }
            }
            if (files.isNotEmpty()){
                Log.d("kpchuck", files[0])
                onReturnListener?.invoke(File(files[0]))
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun startRenderVideo(){
        parentView.setAllEnabled(false)
        val (snackbar, progressBar) = setupSnackbar()
        snackbar.setText("Cutting Video")
        thread (isDaemon = true){
            renderVideo(snackbar, progressBar)
        }
    }

    private fun renderVideo(snackbar: Snackbar, progressBar: ProgressBar){
        val TAG = "Mp4 Composer Log"
        val videoFile = videoViewer.file
        val editor = VideoEditor(cacheDir, applicationContext)
        Log.d("kpchuck", "Cutting times are ${videoViewer.cuttingTimes}")
        editor.cutVideo(videoFile, "/sdcard/mynewfile.mp4", videoViewer.cuttingTimes) {
            Log.d(TAG, "Cut video is combined. Starting to compose final video")
            runOnUiThread {
                snackbar.setText("Adding Effects")
                progressBar.progress = 0
            }
            val filterGroup = ArrayList<GlFilter>()

            for (v in videoViewer.frameMap.keys) {
                filterGroup.add(GlDrawerFilter(v.getDrawer()))
            }
            if (filterGroup.isEmpty()){
                onComplete(snackbar)
                return@cutVideo
            }
            Mp4Composer("/sdcard/mynewfile.mp4", "/sdcard/myeditedfile.mp4")
                .size(videoViewer.dimens.first, videoViewer.dimens.second)
                .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                .filter(GlFilterGroup(filterGroup))
                .listener(object : Mp4Composer.Listener {
                    override fun onProgress(progress: Double) {
                        Log.d(TAG, "onProgress = $progress")
                        runOnUiThread {
                            progressBar.progress = (progress * 100).toInt()
                        }
                    }

                    override fun onCompleted() {
                        onComplete(snackbar)
                    }

                    override fun onCanceled() {
                        Log.d(TAG, "onCanceled")

                    }

                    override fun onFailed(exception: Exception?) {
                        Log.e(TAG, "onFailed()", exception)

                    }
                })
                .start()
        }
    }

    private fun onComplete(snackbar: Snackbar){
        runOnUiThread {
            Toast.makeText(applicationContext, "Rendering Complete", Toast.LENGTH_SHORT).show()
            snackbar.dismiss()
            Log.d("Render Complete", "onCompleted()")
            parentView.setAllEnabled(true)
        }
    }

    private fun setupSnackbar(): Pair<Snackbar, ProgressBar> {
        val bar = Snackbar.make(parentView, "Preparing to Render Video", Snackbar.LENGTH_INDEFINITE)
        val item = ProgressBar(applicationContext, null, android.R.attr.progressBarStyleHorizontal)
        item.max = 100
        item.progress = 0
        (bar.view as ViewGroup).addView(item)
        bar.show()
        return Pair(bar, item)
    }

    private fun View.setAllEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (this is ViewGroup) children.forEach { child -> child.setAllEnabled(enabled) }
    }

}
