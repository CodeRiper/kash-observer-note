package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class VoiceRecordingManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null

    fun startRecording(): String? {
        return try {
            stopRecording()
            val outputFile = File(context.cacheDir, "voice_memo_${System.currentTimeMillis()}.3gp")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            
            currentRecordingFile = outputFile
            outputFile.absolutePath
        } catch (e: Exception) {
            Log.e("VoiceRecordingManager", "Error starting recording: ${e.message}", e)
            mediaRecorder?.release()
            mediaRecorder = null
            null
        }
    }

    fun stopRecording(): String? {
        return try {
            mediaRecorder?.let {
                it.stop()
                it.release()
            }
            mediaRecorder = null
            currentRecordingFile?.absolutePath
        } catch (e: Exception) {
            Log.e("VoiceRecordingManager", "Error stopping recording: ${e.message}", e)
            mediaRecorder?.release()
            mediaRecorder = null
            currentRecordingFile?.absolutePath
        }
    }

    fun startPlayback(filePath: String, onComplete: () -> Unit = {}) {
        try {
            stopPlayback()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    onComplete()
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("VoiceRecordingManager", "Error starting playback: ${e.message}", e)
            stopPlayback()
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("VoiceRecordingManager", "Error stopping playback: ${e.message}", e)
            mediaPlayer = null
        }
    }
}
