package com.example.videoapp


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.example.videoapp.databinding.ActivityExoplayerBinding
import com.example.videoapp.databinding.AlertDialogSpeedBinding
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.util.Log


class ExoplayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExoplayerBinding

    private var mPlayer: SimpleExoPlayer? = null
    private var playPauseButton: ImageButton? = null
    private var img15BackButton: AppCompatImageView? = null
    private var img15Button: AppCompatImageView? = null

    // Speed
    var tvSpeedSize: TextView? = null
    private var playbackSpeed = 1.0f
    private var lastSelectedRadioButtonId = -1

    // Duration
    private lateinit var textViewCurrentTime: TextView
    private lateinit var textViewRemainingTime: TextView
    private var isPlaying: Boolean = false
    private val handler = Handler()

    private lateinit var imgLock: AppCompatImageView
    private lateinit var llLock: LinearLayoutCompat
    private lateinit var rlPlayPush: RelativeLayout
    private lateinit var llTimeBar: RelativeLayout
    private lateinit var rlBottomLayout: RelativeLayout
    private var playbackPosition: Long = 0

    var videoURL: String = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
    var pauseTime: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExoplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView(){

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)


        initPlayer()

        img15Button = findViewById(R.id.img15)
        img15BackButton = findViewById(R.id.img15Back)
        tvSpeedSize = findViewById(R.id.tvSpeedSize)

        // Duration
        textViewCurrentTime = findViewById(R.id.textViewCurrentTime)
        textViewRemainingTime = findViewById(R.id.textViewRemainingTime)

        // Lock
        imgLock = findViewById(R.id.imgLock)
        llLock = findViewById(R.id.llLock)
        rlPlayPush = findViewById(R.id.rlPlayPush)
        llTimeBar = findViewById(R.id.llTimeBar)
        rlBottomLayout = findViewById(R.id.rlBottomLayout)


        llLock.setOnClickListener {
            imgLock.visibility = View.VISIBLE
            llLock.visibility = View.GONE
            rlPlayPush.visibility = View.GONE
            llTimeBar.visibility = View.GONE
            rlBottomLayout.visibility = View.GONE
        }

        imgLock.setOnClickListener {
            imgLock.visibility = View.GONE
            llLock.visibility = View.VISIBLE
            rlPlayPush.visibility = View.VISIBLE
            llTimeBar.visibility = View.VISIBLE
            rlBottomLayout.visibility = View.VISIBLE
        }

        val llSpeed = findViewById<LinearLayoutCompat>(R.id.llSpeed)
        llSpeed.setOnClickListener {
            openSpeedDialog()
        }

        img15BackButton?.setOnClickListener {
            mPlayer?.seekTo(mPlayer?.currentPosition!! - 15000)
        }

        img15Button?.setOnClickListener {
            mPlayer?.seekTo(mPlayer?.currentPosition!! + 15000)
        }

        playPauseButton = findViewById(R.id.exoPlayPause)
        playPauseButton?.visibility = View.INVISIBLE

        playPauseButton?.setOnClickListener {
            if (mPlayer != null) {
                if (mPlayer!!.isPlaying) {
                    mPlayer!!.pause()
                    playPauseButton?.setImageResource(R.drawable.ic_play)
                } else {
                    mPlayer!!.play()
                    playPauseButton?.setImageResource(R.drawable.ic_push)
                }
            }
        }
    }


    private fun initPlayer() {
        if (mPlayer == null) {
            mPlayer = SimpleExoPlayer.Builder(this, DefaultRenderersFactory(this)).build()
            binding.playerView.player = mPlayer
        }

        val mediaItem = MediaItem.Builder()
                .setUri(videoURL)
                .build()

            MediaItem.fromUri(videoURL)
        mPlayer!!.setMediaItem(mediaItem)
        mPlayer!!.playWhenReady = true

        if (pauseTime.isNotEmpty())
            playbackPosition = pauseTime.toLong()

        if (playbackPosition > 0) {
            mPlayer!!.seekTo(playbackPosition)
        }

        // With Duration
        videoListener.let { binding.playerView.player?.removeListener(it) }
        binding.playerView.player?.addListener(videoListener)

        playVideo()
    }

    private val videoListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            Log.e("TAG", "onPlaybackStateChanged: $playbackState")
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    playPauseButton?.visibility = View.INVISIBLE
                }

                Player.STATE_READY -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    playPauseButton?.visibility = View.VISIBLE
                    if (isPlaying) {
                        handler.post(updateRunnable)
                    } else {
                        handler.removeCallbacks(updateRunnable)
                    }
                    val duration = binding.playerView.player?.duration ?: 0
                    val currentPosition = binding.playerView.player?.currentPosition ?: 0
                    val remainingTime = duration - currentPosition

                    textViewCurrentTime.text = formatTime(currentPosition)
                    textViewRemainingTime.text = formatTime(remainingTime)
                }

                Player.STATE_ENDED -> {

                }

                else -> {
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                playPauseButton?.setImageResource(R.drawable.ic_push)

                handler.post(updateRunnable)
            } else {
                playPauseButton?.setImageResource(R.drawable.ic_play)
                handler.removeCallbacks(updateRunnable)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e("TAG", "TYPE_SOURCE: " + error.message)
        }

        override fun onCues(cues: MutableList<Cue>) {
            super.onCues(cues)
            Log.e("TAG", "onCues: $cues")
        }

        override fun onMetadata(metadata: Metadata) {
            super.onMetadata(metadata)
            Log.e("TAG", "onMetadata: $metadata")
        }

    }

    private fun playVideo() {
        val mediaItem = MediaItem.Builder()
                .setUri(videoURL)
                .build()

            MediaItem.fromUri(videoURL)

        mPlayer!!.setMediaItem(mediaItem)

        mPlayer!!.prepare()
        mPlayer?.seekTo(playbackPosition)
    }

    private fun releasePlayer() {
        if (mPlayer == null) {
            return
        }
        mPlayer!!.release()
        mPlayer = null
    }

    override fun onStart() {
        super.onStart()
        Log.e("TAG", "onStart: ")
        initPlayer()
        if (playbackPosition > 0) {
            mPlayer?.seekTo(playbackPosition)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.e("TAG", "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
        Log.e("TAG", "onStop:")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("TAG", "onDestroy: ")
    }

    override fun onBackPressed() {
        if (mPlayer != null) {
            mPlayer!!.stop()
            releasePlayer()
        }
        val resultIntent = Intent()
        resultIntent.putExtra("playbackPosition", playbackPosition)
        setResult(RESULT_OK, resultIntent)
        Log.e("TAG", "onBackPressed: $playbackPosition")
        super.onBackPressed()
    }

    // Duration
    private val updateRunnable = object : Runnable {
        override fun run() {
            val duration = binding.playerView.player?.duration ?: 0
            val currentPosition = binding.playerView.player?.currentPosition ?: 0
            val remainingTime = duration - currentPosition

            playbackPosition = currentPosition
            textViewCurrentTime.text = formatTime(currentPosition)
            textViewRemainingTime.text = formatTime(remainingTime)

            handler.postDelayed(this, 1000)
        }
    }


    // Duration
    private fun formatTime(timeMs: Long): String {
        val minutes = (timeMs / 1000 / 60).toInt()
        val seconds = (timeMs / 1000 % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun openSpeedDialog() {
        val dialogBinding = AlertDialogSpeedBinding.inflate(LayoutInflater.from(this))

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogBinding.root)

        val alertDialog = alertDialogBuilder.create()
        alertDialog.window?.attributes?.width = ((resources.displayMetrics.widthPixels * 0.9).toInt())

        dialogBinding.radioGroupSpeed.setOnCheckedChangeListener { _, checkedId ->
            updatePlaybackSpeed(checkedId)
            //   saveRadioButtonId(checkedId)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }


    private fun updatePlaybackSpeed(checkedId: Int) {
        when (checkedId) {
            R.id.radio5X -> setPlaybackSpeed(0.5f, "(0.5x)", checkedId)
            R.id.radio75X -> setPlaybackSpeed(0.75f, "(0.75x)", checkedId)
            R.id.radio1X -> setPlaybackSpeed(1.0f, "(1x Normal)", checkedId)
            R.id.radio125X -> setPlaybackSpeed(1.25f, "(1.25x)", checkedId)
            R.id.radio15X -> setPlaybackSpeed(1.5f, "(1.5x)", checkedId)
        }
    }

    private fun setPlaybackSpeed(speed: Float, displayText: String, checkedId: Int) {
        playbackSpeed = speed
        tvSpeedSize?.text = displayText
        mPlayer?.playbackParameters = PlaybackParameters(playbackSpeed)
        lastSelectedRadioButtonId = checkedId
    }

}

