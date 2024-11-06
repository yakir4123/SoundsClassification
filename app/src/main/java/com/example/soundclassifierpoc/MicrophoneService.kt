package com.example.soundclassifierpoc

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import convertPcmToWav
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MicrophoneService : Service() {

    private var isRecording = false
    private lateinit var audioRecord: AudioRecord
    private val bufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    companion object {
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startForegroundService()
        startRecordingLoop()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    private fun startForegroundService() {
        // Create a notification channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "MicrophoneServiceChannel"
            val channel = NotificationChannel(
                channelId,
                "Microphone Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Microphone Service")
                .setContentText("Listening to the microphone...")
                .build()

            startForeground(1, notification)
        }
    }

    private fun startRecordingLoop() {
        isRecording = true
        Thread {
            while (isRecording) {
                recordForOneMinute()
            }
        }.start()
    }

    private fun recordForOneMinute() {
        // Initialize AudioRecord
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        // Start recording
        audioRecord.startRecording()
        val audioData = ByteArray(bufferSize)

        val pcmFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audio_record.pcm")
        val wavFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audio_record.wav")

        try {
            FileOutputStream(pcmFile).use { outputStream ->
                var startTime = System.currentTimeMillis()
                while (isRecording && (System.currentTimeMillis() - startTime < 5_000)) {
                    val read = audioRecord.read(audioData, 0, audioData.size)
                    if (read > 0) {
                        outputStream.write(audioData, 0, read)
                    }
                }
            }

            // Convert the recorded PCM file to WAV
            convertPcmToWav(pcmFile, wavFile, sampleRate = 44100, channels = 1, bitDepth = 16)

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
