package com.example.videoapp

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.example.videoapp.databinding.ActivityPlayVideoBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class PlayVideoActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityPlayVideoBinding

    private lateinit var player: ExoPlayer
    private var exoPlayPush: ImageButton? = null
    private var ivBackPlayVideo: AppCompatImageView? = null

    private var videoUrl : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exoPlayPush = findViewById(R.id.exoPlayPause)
        ivBackPlayVideo = findViewById(R.id.ivBackPlayVideo)

        exoPlayPush?.setOnClickListener(this)
        ivBackPlayVideo?.setOnClickListener(this)

        initView()
    }

    private fun initView() {

        if (intent != null){
            videoUrl = intent.getStringExtra("video_url").toString()
        }
        initializePlayer()
    }


    override fun onClick(view: View) {
        when (view.id) {

            R.id.ivBackPlayVideo -> onBackPressed()

            R.id.exoPlayPause -> togglePlayPause()
        }

    }

    private fun initializePlayer() {
        binding.progressBar.visibility = View.VISIBLE
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        val uri = Uri.parse(videoUrl)
        //val mediaItem = MediaItem.fromUri(uri)  // TODO :: For dynamic

        val rawVideoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.real_pain_story_1)
        val mediaItem = MediaItem.fromUri(rawVideoUri)


        player.addListener(object : Player.Listener {
            override fun onIsLoadingChanged(isLoading: Boolean) {
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            override fun onPlayerError(error: PlaybackException) {
                binding.progressBar.visibility = View.GONE
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE, Player.STATE_ENDED -> {
                        binding.progressBar.visibility = View.GONE
                        exoPlayPush?.setImageResource(R.drawable.ic_play)
                        if (playbackState == Player.STATE_ENDED) {
                            player.seekTo(0)
                            player.playWhenReady = false
                        }
                    }

                    Player.STATE_READY -> {
                        binding.progressBar.visibility = View.GONE
                        if (player.playWhenReady) {
                            exoPlayPush?.setImageResource(R.drawable.ic_push)
                        } else {
                            exoPlayPush?.setImageResource(R.drawable.ic_play)
                        }
                    }
                }
            }

        })

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

    }

    private fun togglePlayPause() {
        if (player.playbackState == Player.STATE_ENDED) {
            player.seekTo(0)
            player.play()
            exoPlayPush?.setImageResource(R.drawable.ic_push)
        } else {
            if (player.isPlaying) {
                player.pause()
                exoPlayPush?.setImageResource(R.drawable.ic_play)
            } else {
                player.play()
                exoPlayPush?.setImageResource(R.drawable.ic_push)
            }
        }
    }


    private fun releasePlayer() {
        player.release()
    }

    override fun onPause() {
        super.onPause()
        player.pause() // Pause playback when activity is paused
    }

    override fun onStop() {
        super.onStop()
        releasePlayer() // Release player when activity is stopped
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer() // Ensure player is released when activity is destroyed
    }
}