package com.example.videoapp



import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.videoapp.databinding.ActivityExoplayerBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.text.CueGroup
import com.google.android.exoplayer2.text.TextOutput
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.common.collect.ImmutableList

class Dummy : AppCompatActivity(), TextOutput {

    private lateinit var binding: ActivityExoplayerBinding


    private var mPlayer: SimpleExoPlayer? = null
    private var playPauseButton: ImageButton? = null
    private var img15BackButton: AppCompatImageView? = null
    private var img15Button: AppCompatImageView? = null
    private var tvEpisode: AppCompatTextView? = null

    // Speed
    var tvSpeedSize: TextView? = null
    private var playbackSpeed = 1.0f
    private var lastSelectedRadioButtonId = -1

    // Subtitle
    var llSubTitle: LinearLayoutCompat? = null
    var tvSubTitle: TextView? = null
    private var selectedSubtitleText: String = ""

    // Duration
    private lateinit var textViewCurrentTime: TextView
    private lateinit var textViewRemainingTime: TextView
    private var isPlaying: Boolean = false
    private val handler = Handler()

    private lateinit var imgLock: AppCompatImageView
    private lateinit var llLock: LinearLayoutCompat
    private lateinit var llToolBar: LinearLayoutCompat
    private lateinit var greenbackPlayer: AppCompatImageView
    private lateinit var rlPlayPush: RelativeLayout
    private lateinit var llTimeBar: RelativeLayout
    private lateinit var rlBottomLayout: RelativeLayout

    private var playbackPosition: Long = 0

    // VM

    var mediaSubTitleUrl: String = ""
    var videoURL: String = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"

    // Constant
    var subscriptionId: String = ""
    var seriesId: String = ""
    var pauseTime: String = ""

    // Global data structure to store the mapping between downloadId and URL
    val downloadIdUrlMapping = mutableMapOf<String, Long>()
    // val commonSessionList = ArrayList<SeasonListResponse.Payload>()
    val commonEpisodeList = ArrayList<EpisodeListResponse.Payload>()
    var subTitlesList = ArrayList<HomeMediaDetailResponse2.Payload.Subtitle>()
    var subtitlePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExoplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView(){

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        // init the subtitle url
        /*  val subTitleLanguageItem = Constants.subTitlesList.find { it.language.equals(getSubTitleLanguage(), true) }
        subTitleLanguageItem?.let {
            mediaSubTitleUrl = Constants.subtitlePath + "/" + it.file
            selectedSubtitleText = it.language
            tvSubTitle?.text = selectedSubtitleText
        }*/

        initPlayer()

        // TODO : Resume


        img15Button = findViewById(R.id.img15)
        img15BackButton = findViewById(R.id.img15Back)
        tvSpeedSize = findViewById(R.id.tvSpeedSize)
        tvSubTitle = findViewById(R.id.tvSubTitle)

        // Duration
        textViewCurrentTime = findViewById(R.id.textViewCurrentTime)
        textViewRemainingTime = findViewById(R.id.textViewRemainingTime)

        // Lock
        imgLock = findViewById(R.id.imgLock)
        llLock = findViewById(R.id.llLock)
        rlPlayPush = findViewById(R.id.rlPlayPush)
        llTimeBar = findViewById(R.id.llTimeBar)
        rlBottomLayout = findViewById(R.id.rlBottomLayout)

        greenbackPlayer.setOnClickListener { onBackPressed() }

        llLock.setOnClickListener {
            imgLock.visibility = View.VISIBLE
            llLock.visibility = View.GONE
            greenbackPlayer.visibility = View.GONE
            rlPlayPush.visibility = View.GONE
            llTimeBar.visibility = View.GONE
            rlBottomLayout.visibility = View.GONE
            llToolBar.visibility = View.GONE
        }

        imgLock.setOnClickListener {
            imgLock.visibility = View.GONE
            llLock.visibility = View.VISIBLE
            greenbackPlayer.visibility = View.VISIBLE
            rlPlayPush.visibility = View.VISIBLE
            llTimeBar.visibility = View.VISIBLE
            rlBottomLayout.visibility = View.VISIBLE
            llToolBar.visibility = View.VISIBLE
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

        /*  if (getAutoPlayPreview()) {
              playPauseButton?.setImageResource(R.drawable.ic_push)
          } else {
              playPauseButton?.setImageResource(R.drawable.ic_play)
          }*/

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

        // Set for Subtitle
        /*     val videoURL = if (viewModel.fromHome.value == true) {
                viewModel.fromHomeVideoURL.value.toString()
            } else if (viewModel.fromEpisode.value == true) {
                viewModel.fromEpisodeVideo.value.toString()
            } else if (viewModel.fromCollection.value == true) {
                viewModel.fromCollectionVideo.value.toString()
            } else if (viewModel.fromDownload.value == true) {
                viewModel.fromDownloadVideo.value.toString()
            } else if (viewModel.fromTrailerAndMore.value == true) {
                viewModel.fromTrailerAndMoreVideo.value.toString()
            } else if (viewModel.fromMoreLike.value == true) {
                viewModel.fromMoreLikeVideo.value.toString()
            } else {
                viewModel.videoURL.value.toString()
            }  */

        val mediaItem = if (mediaSubTitleUrl.isNotEmpty()) {
            val subTitleUri = Uri.parse(mediaSubTitleUrl)

            // Subtitle configuration
            val subtitle = MediaItem.SubtitleConfiguration.Builder(subTitleUri)
                .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                .setLanguage("en")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()

            MediaItem.Builder()
                .setUri(videoURL)
                .setSubtitleConfigurations(ImmutableList.of(subtitle))
                .build()
        } else {
            MediaItem.fromUri(videoURL)
        }
        mPlayer!!.setMediaItem(mediaItem)
        mPlayer!!.playWhenReady = true
        //   mPlayer!!.playWhenReady = getAutoPlayPreview()

        if (pauseTime.isNotEmpty())
            playbackPosition = pauseTime.toLong()

        if (playbackPosition > 0) {
            mPlayer!!.seekTo(playbackPosition)
        }

        // With Duration
        videoListener.let {
            binding.playerView.player?.removeListener(it)
        }
        binding.playerView.player?.addListener(videoListener)

        // Set for Subtitle
        /*    viewModel.videoURL.value = if (viewModel.fromHome.value == true) {
                viewModel.fromHomeVideoURL.value.toString()
            } else if (viewModel.fromEpisode.value == true) {
                viewModel.fromEpisodeVideo.value.toString()
            } else if (viewModel.fromCollection.value == true) {
                viewModel.fromCollectionVideo.value.toString()
            } else if (viewModel.fromDownload.value == true) {
                viewModel.fromDownloadVideo.value.toString()
            } else if (viewModel.fromTrailerAndMore.value == true) {
                viewModel.fromTrailerAndMoreVideo.value.toString()
            } else if (viewModel.fromMoreLike.value == true) {
                viewModel.fromMoreLikeVideo.value.toString()
            } else {
                viewModel.videoURL.value.toString()
            }*/

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

                    /*    if (viewModel.mediaType.value == "movie") {
                            if (getAutoPlayPreview()) { // Boolean
                                mPlayer!!.seekTo(0)
                                mPlayer!!.play()
                            } else {
                                isPlaying = false
                                mPlayer!!.seekTo(0)
                                mPlayer!!.pause()
                                playPauseButton?.setImageResource(R.drawable.ic_play)
                            }
                        } else {
                            // Series or Episode
                            Log.e("TAG", "playNextVideo() - 1")
                            if (getAutoPlayNextEpisode()) { // Boolean
                                playNextVideo()
                            }
                        }  */

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
        val mediaItem = if (mediaSubTitleUrl.isNotEmpty()) {
            val subTitleUri = Uri.parse(mediaSubTitleUrl)
            // Subtitle configuration
            val subtitle = MediaItem.SubtitleConfiguration.Builder(subTitleUri)
                .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                .setLanguage("en")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()

            MediaItem.Builder()
                .setUri(videoURL)
                .setSubtitleConfigurations(ImmutableList.of(subtitle))
                .build()
        } else {
            MediaItem.fromUri(videoURL)
        }
        mPlayer!!.setMediaItem(mediaItem)

        mPlayer!!.prepare()
        mPlayer?.seekTo(playbackPosition)
    }

    fun playNextVideo() {
        if (commonEpisodeList.isNotEmpty()) {
            // Determine the index of the next video
            val currentIndex = commonEpisodeList.indexOfFirst { it.video == videoURL }
            Log.e("playNextVideo", "currentIndex... $currentIndex")
            val nextIndex = (currentIndex + 1) % commonEpisodeList.size
            Log.e("playNextVideo", "nextIndex.... $nextIndex")
            // Prepare and play the next video
            videoURL = commonEpisodeList[nextIndex]!!.video
            tvEpisode?.text = "Episode " + commonEpisodeList[nextIndex]!!.episodeNo

            playbackPosition = 0
            playVideo()

            if (nextIndex == commonEpisodeList.size - 1) {
                mPlayer?.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        if (playbackState == Player.STATE_ENDED) {
                            mPlayer?.pause()
                            mPlayer?.removeListener(this)
                        }
                    }
                })
            }

            /*    if (getAutoPlayNextEpisode()) {
                    mPlayer?.play()
                } */

        } else {
            Log.e("playNextVideo", "Episode list is empty.")
            Toast.makeText(this, "No more videos available.", Toast.LENGTH_SHORT).show()
        }
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
        //playbackPosition = mPlayer?.currentPosition ?: 0
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
        //   resultIntent.putExtra("videoId", viewModel.mediaId.value.toString())
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
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_speed, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)

        val alertDialog = alertDialogBuilder.create()
        alertDialog.window?.attributes?.width = ((resources.displayMetrics.widthPixels * 0.9).toInt())
        //    alertDialog.window?.setBackgroundDrawableResource(R.drawable.dr_profile_5)

        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupSpeed)

        /*    val lastSelectedId = getSavedRadioButtonId()  // Int
            radioGroup.check(lastSelectedId)
            updatePlaybackSpeed(lastSelectedId)  */

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            updatePlaybackSpeed(checkedId)
            //   saveRadioButtonId(checkedId) // Int
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

    private fun openSubTitle() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_subtitle, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)

        val alertDialog = alertDialogBuilder.create()
        alertDialog.window?.attributes?.width = ((resources.displayMetrics.widthPixels * 0.9).toInt())
        // alertDialog.window?.setBackgroundDrawableResource(R.drawable.dr_profile_5)

        val clSubTitle = dialogView.findViewById<ConstraintLayout>(R.id.clSubTitle)
        val rvSubtitle = dialogView.findViewById<RecyclerView>(R.id.rvSubtitle)

        clSubTitle.visibility = View.VISIBLE

        // Set up RecyclerView
        val subtitleList = subTitlesList

        /*   rvSubtitle.setUpRecyclerView_Binding<HomeMediaDetailResponse2.Payload.Subtitle, RowSubtitleBinding>(
               R.layout.row_subtitle, subtitleList, RecyclerViewLayoutManager.LINEAR, RecyclerViewLinearLayout.VERTICAL
           ) {
               contentBinder { item, binding, i ->
                   binding.radioSubtitle.text = item.language
                   binding.radioSubtitle.isChecked = item.language == selectedSubtitleText

                   binding.radioSubtitle.setOnClickListener {
                       setSubTitleLanguage(item.language)
                       selectedSubtitleText = item.language
                       viewModel.mediaSubTitleUrl.value = Constants.subtitlePath + "/" + item.file
                       tvSubTitle?.text = selectedSubtitleText
                       playbackPosition = mPlayer?.currentPosition ?: 0
                       playVideo()

                       alertDialog.dismiss()

                   }
               }
           }  */

        alertDialog.show()
    }

    /* @SuppressLint("MissingInflatedId")
    private fun openEpisodeBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.fragment_episode, null)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rvEpisode: RecyclerView = view.findViewById(R.id.rvEpisode)
        val spinnerSession: Spinner = view.findViewById(R.id.spinnerSession)
        val tvNoEpisode: AppCompatTextView = view.findViewById(R.id.tvNoEpisode)

        // TODO : Set Series List Data
        Constants.commonSessionList.let { response ->
            val titles = response.map { it.title }
            val sessionIds = response.map { it.id }
            val adapter = ArrayAdapter(this@VideoPlayerActivity, R.layout.spinner_item_episode, titles)
            adapter.setDropDownViewResource(R.layout.drop_down_item_episode)

            spinnerSession.adapter = adapter

            spinnerSession.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long,
                ) {
                    val selectedTitle = titles[position]
                    viewModel.sessionId.value = sessionIds[position]
                    viewModel.selectedSession.value = selectedTitle

                    // TODO : Call Episode List API
                    if (this@VideoPlayerActivity.isNetworkConnected()) {
                        lifecycleScope.launch {
                            viewModel.episodeListRequest(
                                "EN",
                                viewModel.selectedSeriesId.value.toString(),
                                viewModel.sessionId.value.toString(),
                                getLoginToken().toString()
                            )
                        }

                        viewModel.episodeListResponse.observe(this@VideoPlayerActivity) { response ->
                            if (response?.payload?.size == 0) {
                                tvNoEpisode.visibility = View.VISIBLE
                                rvEpisode.visibility = View.GONE
                            } else {
                                tvNoEpisode.visibility = View.GONE
                                rvEpisode.visibility = View.VISIBLE

                                viewModel.episodeList.value = response?.payload!!

                                rvEpisode.setUpRecyclerView_Binding<EpisodeListResponse.Payload, RowEpisodePlayerBinding>(
                                    R.layout.row_episode_player,
                                    viewModel.episodeList.value!!,
                                    RecyclerViewLayoutManager.LINEAR,
                                    RecyclerViewLinearLayout.HORIZONTAL
                                ) {
                                    contentBinder { item, binding, i ->
                                        binding.apply {

                                            tvEpisodeNumber.text = "Episode ${item?.episodeNo}"
                                            tvMinutes.text = item?.mainVideoDuration
                                            tvDescription.text = item?.description

                                            Glide.with(this@VideoPlayerActivity).load(item?.defaultImage).error(R.drawable.placeholder)
                                                .into(imageView)

                                            clImageLayout.setOnClickListener {
                                                playbackPosition = 0
                                                viewModel.videoURL.value = item?.video
                                                playVideo()
                                                dialog.dismiss()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        viewModel.showErrorMessage(getString(R.string.internet_connection))
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }
        }

        dialog.behavior.apply {
            isFitToContents = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        dialog.setCancelable(true)
        dialog.setContentView(view)
        dialog.show()
    }  */

    override fun onCues(cueGroup: CueGroup) {
        if (cueGroup.cues.isNotEmpty()) {
            val subtitleText = cueGroup.cues[0].text
            tvSubTitle?.text = subtitleText
        } else {
            tvSubTitle?.text = ""
        }
    }




}