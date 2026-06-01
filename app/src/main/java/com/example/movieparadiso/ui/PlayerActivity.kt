package com.example.movieparadiso.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.movieparadiso.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    private var player: ExoPlayer? = null
    private var videoUri: String? = null
    private var movieTitle: String = "Movie Player"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FullscreenHelper.enableFullscreen(this)

        readIntentData()
        setupClickListeners()
        initializePlayer()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            FullscreenHelper.enableFullscreen(this)
        }
    }

    private fun readIntentData() {
        videoUri = intent.getStringExtra("video_uri")
        movieTitle = intent.getStringExtra("movie_title") ?: "Movie Player"

        binding.tvPlayerTitle.text = movieTitle

        if (videoUri.isNullOrBlank()) {
            Toast.makeText(this, "No video file found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnClosePlayer.setOnClickListener {
            finish()
        }
    }

    private fun initializePlayer() {
        val uriString = videoUri ?: return
        val uri = Uri.parse(uriString)

        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer

            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    override fun onStart() {
        super.onStart()

        if (player == null && !videoUri.isNullOrBlank()) {
            initializePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        binding.playerView.player = null
        player?.release()
        player = null
    }
}