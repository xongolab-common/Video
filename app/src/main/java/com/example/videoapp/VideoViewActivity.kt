package com.example.videoapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.videoapp.databinding.ActivityVideoViewBinding

class VideoViewActivity : AppCompatActivity(), View.OnClickListener  {
    private lateinit var binding: ActivityVideoViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView(){
        val videoView: VideoView = binding.videoView
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.splash_video2)

        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.setOnVideoSizeChangedListener { _, _, _ ->
                // Stretch the video to fit the screen
                videoView.layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                videoView.layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
                videoView.layoutParams = videoView.layoutParams
            }
        }

        videoView.setOnCompletionListener {
            startActivity(Intent(this@VideoViewActivity, MainActivity::class.java))
            finish()
        }

        videoView.start()
    }

    override fun onClick(view: View) {
        when (view.id){

        }
    }
}