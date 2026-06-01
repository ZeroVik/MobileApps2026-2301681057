package com.example.movieparadiso.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.movieparadiso.databinding.ActivityVlcPlayerBinding
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.io.File
import java.io.FileOutputStream
import androidx.appcompat.app.AlertDialog

class VlcPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVlcPlayerBinding

    private var libVLC: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private var surfaceView: SurfaceView? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null

    private var videoUri: String? = null
    private var movieTitle: String = "VLC Player"

    private val handler = Handler(Looper.getMainLooper())
    private var isUserSeeking = false
    private var controlsVisible = true

    private var isChoosingSubtitle = false
    private var shouldResumeAfterSubtitlePicker = false
    private var cachedSubtitleFile: File? = null

    private val subtitlePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            isChoosingSubtitle = false

            if (uri != null) {
                handleSubtitleSelection(uri)
            } else {
                Toast.makeText(this, "No subtitles selected", Toast.LENGTH_SHORT).show()

                val currentTime = mediaPlayer?.time ?: 0L
                val shouldAutoPlay = shouldResumeAfterSubtitlePicker

                restartPlayerAt(currentTime, shouldAutoPlay)
            }

            FullscreenHelper.enableFullscreen(this)
        }

    private val progressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        super.onCreate(savedInstanceState)

        binding = ActivityVlcPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FullscreenHelper.enableFullscreen(this)

        readIntentData()
        setupClickListeners()
        setupSeekBar()

        binding.vlcVideoContainer.post {
            setupVlcPlayer(startPosition = 0L, autoPlay = true)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            FullscreenHelper.enableFullscreen(this)
        }
    }

    private fun readIntentData() {
        videoUri = intent.getStringExtra("video_uri")
        movieTitle = intent.getStringExtra("movie_title") ?: "VLC Player"

        binding.tvVlcPlayerTitle.text = movieTitle

        if (videoUri.isNullOrBlank()) {
            Toast.makeText(this, "No video file found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnCloseVlcPlayer.setOnClickListener {
            finish()
        }

        binding.btnChooseSubtitle.setOnClickListener {
            openSubtitlePicker()
        }

        binding.btnChooseAudioTrack.setOnClickListener {
            showAudioTrackDialog()
        }

        binding.btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        binding.btnRewind.setOnClickListener {
            seekBy(-10_000)
        }

        binding.btnForward.setOnClickListener {
            seekBy(10_000)
        }

        binding.vlcPlayerRoot.setOnClickListener {
            toggleControls()
        }

        binding.vlcVideoContainer.setOnClickListener {
            toggleControls()
        }
    }

    private fun openSubtitlePicker() {
        val player = mediaPlayer

        if (player == null) {
            Toast.makeText(this, "Player is not ready yet", Toast.LENGTH_SHORT).show()
            return
        }

        isChoosingSubtitle = true
        shouldResumeAfterSubtitlePicker = player.isPlaying

        if (player.isPlaying) {
            player.pause()
            binding.btnPlayPause.text = "▶"
        }

        subtitlePickerLauncher.launch(
            arrayOf(
                "text/*",
                "application/x-subrip",
                "application/octet-stream",
                "*/*"
            )
        )
    }

    private fun showAudioTrackDialog() {
        val player = mediaPlayer

        if (player == null) {
            Toast.makeText(this, "Player is not ready yet", Toast.LENGTH_SHORT).show()
            return
        }

        val audioTracks = try {
            player.audioTracks
        } catch (_: Exception) {
            null
        }

        if (audioTracks == null || audioTracks.isEmpty()) {
            Toast.makeText(this, "No audio tracks found", Toast.LENGTH_SHORT).show()
            return
        }

        val currentAudioTrackId = try {
            player.audioTrack
        } catch (_: Exception) {
            -1
        }

        val trackNames = audioTracks.mapIndexed { index, track ->
            val cleanName = track.name ?: "Audio track ${index + 1}"

            if (track.id == currentAudioTrackId) {
                "✓ $cleanName"
            } else {
                cleanName
            }
        }.toTypedArray()

        val checkedItem = audioTracks.indexOfFirst { it.id == currentAudioTrackId }

        AlertDialog.Builder(this)
            .setTitle("Choose audio track")
            .setSingleChoiceItems(trackNames, checkedItem) { dialog, which ->
                val selectedTrack = audioTracks[which]

                val changed = try {
                    player.audioTrack = selectedTrack.id
                    true
                } catch (_: Exception) {
                    false
                }

                if (changed) {
                    binding.btnChooseAudioTrack.text = "AUD✓"
                    Toast.makeText(this, "Audio track changed", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to change audio track", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
                FullscreenHelper.enableFullscreen(this)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                FullscreenHelper.enableFullscreen(this)
            }
            .show()
    }

    private fun setupSeekBar() {
        binding.seekBarPlayer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val player = mediaPlayer ?: return
                    val length = player.length

                    if (length > 0) {
                        val newTime = (length * progress) / 1000
                        binding.tvCurrentTime.text = formatTime(newTime)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val player = mediaPlayer ?: return
                val length = player.length

                if (length > 0) {
                    val progress = seekBar?.progress ?: 0
                    player.time = (length * progress) / 1000
                }

                isUserSeeking = false
            }
        })
    }

    private fun setupVlcPlayer(startPosition: Long, autoPlay: Boolean) {
        val uriString = videoUri ?: return
        val uri = Uri.parse(uriString)

        try {
            val isRemote = isRemoteUri(uriString)

            val fileDescriptor = if (isRemote) {
                null
            } else {
                parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                parcelFileDescriptor?.fileDescriptor
            }

            if (!isRemote && fileDescriptor == null) {
                Toast.makeText(this, "Cannot open selected video file", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            val options = arrayListOf(
                "--avcodec-hw=any",
                "--file-caching=1500",
                "--network-caching=2000",
                "--clock-jitter=0",
                "--clock-synchro=0"
            )

            libVLC = LibVLC(this, options)

            surfaceView = SurfaceView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            binding.vlcVideoContainer.removeAllViews()
            binding.vlcVideoContainer.addView(surfaceView)

            mediaPlayer = MediaPlayer(libVLC).apply {
                vlcVout.setVideoView(surfaceView)

                val width = binding.vlcVideoContainer.width
                val height = binding.vlcVideoContainer.height

                vlcVout.setWindowSize(width, height)
                vlcVout.attachViews()

                aspectRatio = null
                scale = 0f

                setEventListener { event ->
                    when (event.type) {
                        MediaPlayer.Event.Playing -> {
                            runOnUiThread {
                                binding.btnPlayPause.text = "❚❚"
                                startProgressUpdates()
                            }
                        }

                        MediaPlayer.Event.Paused -> {
                            runOnUiThread {
                                binding.btnPlayPause.text = "▶"
                            }
                        }

                        MediaPlayer.Event.Stopped -> {
                            runOnUiThread {
                                binding.btnPlayPause.text = "▶"
                                stopProgressUpdates()
                            }
                        }

                        MediaPlayer.Event.EndReached -> {
                            runOnUiThread {
                                binding.btnPlayPause.text = "▶"
                                stopProgressUpdates()
                            }
                        }

                        MediaPlayer.Event.EncounteredError -> {
                            runOnUiThread {
                                Toast.makeText(
                                    this@VlcPlayerActivity,
                                    "VLC cannot play this file or stream",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }

                val media = if (isRemote) {
                    Media(libVLC, uri).apply {
                        setHWDecoderEnabled(true, false)
                        addOption(":network-caching=2000")

                        cachedSubtitleFile?.let { subtitleFile ->
                            if (subtitleFile.exists()) {
                                addOption(":sub-file=${subtitleFile.absolutePath}")
                            }
                        }
                    }
                } else {
                    Media(libVLC, fileDescriptor).apply {
                        setHWDecoderEnabled(true, false)
                        addOption(":file-caching=1500")

                        cachedSubtitleFile?.let { subtitleFile ->
                            if (subtitleFile.exists()) {
                                addOption(":sub-file=${subtitleFile.absolutePath}")
                            }
                        }
                    }
                }

                setMedia(media)
                media.release()

                play()
            }

            binding.vlcVideoContainer.postDelayed({
                mediaPlayer?.time = startPosition.coerceAtLeast(0L)

                if (!autoPlay) {
                    mediaPlayer?.pause()
                    binding.btnPlayPause.text = "▶"
                } else {
                    binding.btnPlayPause.text = "❚❚"
                }

                FullscreenHelper.enableFullscreen(this)
            }, 700)

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error opening video: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun handleSubtitleSelection(subtitleUri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                subtitleUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
            // Some providers do not support persistable permissions.
        }

        val subtitleFile = copySubtitleToCache(subtitleUri)

        if (subtitleFile == null || !subtitleFile.exists()) {
            Toast.makeText(this, "Cannot load subtitle file", Toast.LENGTH_LONG).show()
            resumeAfterSubtitlePickerIfNeeded()
            return
        }

        cachedSubtitleFile = subtitleFile
        binding.btnChooseSubtitle.text = "CC✓"

        val currentTime = mediaPlayer?.time ?: 0L
        val shouldAutoPlay = shouldResumeAfterSubtitlePicker

        Toast.makeText(this, "Subtitles loaded", Toast.LENGTH_SHORT).show()

        restartPlayerAt(currentTime, shouldAutoPlay)
    }

    private fun copySubtitleToCache(subtitleUri: Uri): File? {
        return try {
            val outputFile = File(cacheDir, "subtitle_${System.currentTimeMillis()}.srt")

            contentResolver.openInputStream(subtitleUri)?.use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            outputFile
        } catch (_: Exception) {
            null
        }
    }

    private fun restartPlayerAt(position: Long, autoPlay: Boolean) {
        stopProgressUpdates()
        releaseVlcPlayer()

        binding.vlcVideoContainer.postDelayed({
            setupVlcPlayer(
                startPosition = position,
                autoPlay = autoPlay
            )
        }, 300)

        shouldResumeAfterSubtitlePicker = false
    }

    private fun resumeAfterSubtitlePickerIfNeeded() {
        val player = mediaPlayer ?: return

        if (shouldResumeAfterSubtitlePicker) {
            player.play()
            binding.btnPlayPause.text = "❚❚"
        }

        shouldResumeAfterSubtitlePicker = false
    }

    private fun togglePlayPause() {
        val player = mediaPlayer ?: return

        if (player.isPlaying) {
            player.pause()
            binding.btnPlayPause.text = "▶"
        } else {
            player.play()
            binding.btnPlayPause.text = "❚❚"
        }
    }

    private fun seekBy(milliseconds: Long) {
        val player = mediaPlayer ?: return
        val length = player.length

        if (length <= 0) {
            return
        }

        val newTime = (player.time + milliseconds).coerceIn(0, length)
        player.time = newTime
        updateProgress()
    }

    private fun updateProgress() {
        val player = mediaPlayer ?: return

        val length = player.length
        val currentTime = player.time

        if (length <= 0 || isUserSeeking) {
            return
        }

        val progress = ((currentTime * 1000) / length).toInt().coerceIn(0, 1000)

        binding.seekBarPlayer.progress = progress
        binding.tvCurrentTime.text = formatTime(currentTime)
        binding.tvDuration.text = formatTime(length)
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun toggleControls() {
        controlsVisible = !controlsVisible

        val visibility = if (controlsVisible) {
            View.VISIBLE
        } else {
            View.GONE
        }

        binding.topGradient.visibility = visibility
        binding.topControls.visibility = visibility
        binding.centerControls.visibility = visibility
        binding.bottomGradient.visibility = visibility
        binding.bottomControls.visibility = visibility

        FullscreenHelper.enableFullscreen(this)
    }

    private fun startProgressUpdates() {
        handler.removeCallbacks(progressRunnable)
        handler.post(progressRunnable)
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacks(progressRunnable)
    }

    override fun onPause() {
        super.onPause()

        if (!isChoosingSubtitle) {
            mediaPlayer?.pause()
            binding.btnPlayPause.text = "▶"
        }
    }

    override fun onStop() {
        super.onStop()

        if (!isChoosingSubtitle) {
            stopProgressUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressUpdates()
        releaseVlcPlayer()
    }

    private fun releaseVlcPlayer() {
        mediaPlayer?.let { player ->
            try {
                player.stop()
                player.vlcVout.detachViews()
                player.release()
            } catch (_: Exception) {
            }
        }

        mediaPlayer = null

        libVLC?.release()
        libVLC = null

        try {
            parcelFileDescriptor?.close()
        } catch (_: Exception) {
        }

        parcelFileDescriptor = null

        binding.vlcVideoContainer.removeAllViews()
        surfaceView = null
    }

    private fun isRemoteUri(uriString: String): Boolean {
        return uriString.startsWith("http://") || uriString.startsWith("https://")
    }
}