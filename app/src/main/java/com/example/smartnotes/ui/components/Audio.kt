package com.example.smartnotes.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
}

class AndroidAudioRecorder(
    private val context: Context
): AudioRecorder {
    private var recorder: MediaRecorder? = null
    private fun createRecorder(): MediaRecorder {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()

            recorder = this
        }
    }

    override fun stop() {
        /*recorder?.stop()
        recorder?.reset()
        recorder = null*/
        try {
            recorder?.stop()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } finally {
            recorder?.release()
            recorder = null
        }
    }
}


interface AudioPlayer {
    fun play(uri: String, onComplete: () -> Unit)
    fun stop()
    fun start(outputFile: File)
}
class AndroidAudioPlayer(
    private val context: Context
): AudioPlayer {
    private var player: MediaPlayer? = null

    override fun start(outputFile: File) {
        MediaPlayer.create(context, outputFile.toUri()).apply {
            player = this
            start()
        }
    }

    override fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

    override fun play(uri: String, onComplete: () -> Unit) {
        // Liberar el anterior si existía
        stop()

        MediaPlayer().apply {
            try {
                // Lógica para detectar si es content:// o ruta local
                if (uri.startsWith("content://")) {
                    setDataSource(context, Uri.parse(uri))
                } else {
                    setDataSource(uri)
                }

                prepare()
                start()

                //Avisar cuando termina
                setOnCompletionListener {
                    onComplete()
                    stop()
                }
                player = this

            } catch (e: Exception) {
                e.printStackTrace()
                onComplete()
            }
        }
    }
}
