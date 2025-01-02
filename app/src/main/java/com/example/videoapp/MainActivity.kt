package com.example.videoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var realStoriesAdapter: RealStoriesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView(){

        binding.btnGif.setOnClickListener(this)
        binding.btnVideoView.setOnClickListener(this)
        binding.btnExoPlayer.setOnClickListener(this)

        realStoriesAdapter = RealStoriesAdapter(this, this)
        binding.rvRealStories.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.rvRealStories.adapter = realStoriesAdapter

        // Load static data
        val staticData = loadStaticData()
        realStoriesAdapter.addData(staticData)

        realStoriesAdapter.onItemClick = { _, item ->
            val intent = Intent(this, PlayVideoActivity::class.java)
            intent.putExtra("video_url", item.video)
            startActivity(intent)
        }

    }

    private fun loadStaticData(): ArrayList<VideoImageModel.VideoImage> {
        return arrayListOf(
            VideoImageModel.VideoImage(
                _id = "1",
                video = "android.resource://${packageName}/${R.raw.real_pain_story}",
                title = "Video 1",
                description = "Description for Video 1",
              //  thumbnail = "android.resource://${packageName}/${R.raw.thumbnail1}" // Use a placeholder if no thumbnail exists
            ),
            VideoImageModel.VideoImage(
                _id = "2",
                video = "android.resource://${packageName}/${R.raw.real_pain_story_1}",
                title = "Video 2",
                description = "Description for Video 2",
             //   thumbnail = "android.resource://${packageName}/${R.raw.thumbnail2}" // Use a placeholder if no thumbnail exists
            )
        )
    }

    override fun onClick(view: View) {
        when (view.id){
            R.id.btnGif -> startActivity(Intent(this, GIFActivity::class.java))
            R.id.btnVideoView -> startActivity(Intent(this, VideoViewActivity::class.java))
            R.id.btnExoPlayer -> startActivity(Intent(this, ExoplayerActivity::class.java))

        }
    }
}