package com.example.soundclassifierpoc

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var startStopButton: Button
    private lateinit var playButton: Button
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startStopButton = findViewById(R.id.startStopButton)
        playButton = findViewById(R.id.playButton)
        checkAndRequestAudioPermission()
        setButton()
        playButton.setOnClickListener {
            playAudio()
        }
    }

    private fun setButton() {
        startStopButton.text = if (MicrophoneService.isRunning) "Stop" else "Start"
        startStopButton.setOnClickListener {
            checkAndRequestAudioPermission()
            toggleService()
        }

    }


    private fun toggleService() {
        val intent = Intent(this, MicrophoneService::class.java)
        if (MicrophoneService.isRunning) {
            // Stop the service
            stopService(intent)
            Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show()
        } else {
            // Start the service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show()
        }
        updateButtonText()
    }

    private fun updateButtonText() {
        startStopButton.text = if (MicrophoneService.isRunning) "Start" else "Stop"
    }

    private fun checkAndRequestAudioPermission() {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Registering the permission result launcher
    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Audio permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Audio permission denied!", Toast.LENGTH_SHORT).show()
            }
        }



    private fun playAudio() {
        // Release any existing MediaPlayer before creating a new one
        mediaPlayer?.release()

        // Locate the recorded audio file
        val audioFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audio_record.pcm")

        if (audioFile.exists()) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFile.absolutePath)
                    prepare() // Prepare the MediaPlayer asynchronously
                    start()
                }
                Toast.makeText(this, "Playing audio...", Toast.LENGTH_SHORT).show()

                mediaPlayer?.setOnCompletionListener {
                    Toast.makeText(this, "Audio playback completed", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Audio file not found", Toast.LENGTH_SHORT).show()
        }
    }
}
