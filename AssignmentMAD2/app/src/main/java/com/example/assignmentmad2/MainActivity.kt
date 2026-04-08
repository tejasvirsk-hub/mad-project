package com.example.assignmentmad2

import android.app.AlertDialog
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var statusText: TextView
    private var mediaPlayer: MediaPlayer? = null
    private var isAudio = false
    private var currentUri: Uri? = null

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentUri = it
            val mimeType = contentResolver.getType(it)
            if (mimeType?.startsWith("audio") == true) {
                setupAudio(it)
            } else if (mimeType?.startsWith("video") == true) {
                setupVideo(it)
            } else {
                statusText.text = "Unsupported file format"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoView = findViewById(R.id.videoView)
        statusText = findViewById(R.id.statusText)

        findViewById<Button>(R.id.btnOpenFile).setOnClickListener {
            filePicker.launch("*/*")
        }

        findViewById<Button>(R.id.btnOpenUrl).setOnClickListener {
            showUrlDialog()
        }

        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            if (isAudio) mediaPlayer?.start() else videoView.start()
        }

        findViewById<Button>(R.id.btnPause).setOnClickListener {
            if (isAudio) mediaPlayer?.pause() else videoView.pause()
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            if (isAudio) {
                mediaPlayer?.stop()
                mediaPlayer?.prepare() // Reset to beginning
            } else {
                videoView.stopPlayback()
                currentUri?.let { videoView.setVideoURI(it) }
            }
        }

        findViewById<Button>(R.id.btnRestart).setOnClickListener {
            if (isAudio) {
                mediaPlayer?.seekTo(0)
                mediaPlayer?.start()
            } else {
                videoView.seekTo(0)
                videoView.start()
            }
        }
    }

    private fun setupAudio(uri: Uri) {
        stopAll()
        isAudio = true
        videoView.visibility = View.GONE
        mediaPlayer = MediaPlayer().apply {
            setDataSource(applicationContext, uri)
            prepare()
        }
        statusText.text = "Playing Audio: ${uri.lastPathSegment}"
    }

    private fun setupVideo(uri: Uri) {
        stopAll()
        isAudio = false
        videoView.visibility = View.VISIBLE
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { 
            statusText.text = "Video Ready: ${uri.lastPathSegment}"
        }
    }

    private fun showUrlDialog() {
        val input = EditText(this)
        input.hint = "https://example.com/video.mp4"
        AlertDialog.Builder(this)
            .setTitle("Stream Video from URL")
            .setView(input)
            .setPositiveButton("Stream") { _, _ ->
                val url = input.text.toString()
                if (url.isNotEmpty()) {
                    val uri = Uri.parse(url)
                    currentUri = uri
                    setupVideo(uri)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun stopAll() {
        mediaPlayer?.release()
        mediaPlayer = null
        videoView.stopPlayback()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAll()
    }
}