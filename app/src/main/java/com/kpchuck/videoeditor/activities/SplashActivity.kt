package com.kpchuck.videoeditor.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import com.kpchuck.videoeditor.EffectVideoBinder
import com.kpchuck.videoeditor.MainActivity
import com.kpchuck.videoeditor.R
import com.kpchuck.videoeditor.controllers.EffectViewController
import com.kpchuck.videoeditor.controllers.SeekBarController
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import kotlinx.android.synthetic.main.activity_splash.*
import views.VideoViewer
import java.io.File

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        newProject.setOnClickListener { startNewProject() }
    }

    private fun startNewProject(){
        FilePickerBuilder.instance.setMaxCount(1)
            .enableVideoPicker(true)
            .enableImagePicker(false)
            .enableCameraSupport(true)
            .setActivityTheme(R.style.LibAppTheme)
            .pickPhoto(this)
    }

    private fun startMainAcitivty(file: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(FILENAME, file)
        intent.putExtra(NEW_PROJECT, true)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null){
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
                startMainAcitivty(files[0])
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        val FILENAME = "filename"
        val NEW_PROJECT = "new_project"
    }


}
