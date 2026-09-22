package com.example.game

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    var isSoundEnabled: Boolean = true
    var isHapticEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private fun playTone(freqs: FloatArray, durationMs: Int, volume: Float = 0.6f) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                val generatedSnd = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val freqIdx = (progress * freqs.size).toInt().coerceIn(0, freqs.size - 1)
                    val freq = freqs[freqIdx]
                    val angle = 2.0 * PI * i / (sampleRate / freq)
                    val envelope = if (progress < 0.1f) progress / 0.1f else (1.0f - progress)
                    val sample = (sin(angle) * envelope * volume * Short.MAX_VALUE).toInt()
                    generatedSnd[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(generatedSnd.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(generatedSnd, 0, generatedSnd.size)
                audioTrack.play()
                audioTrack.setNotificationMarkerPosition(numSamples)
                audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onMarkerReached(track: AudioTrack?) {
                        track?.release()
                    }
                    override fun onPeriodicNotification(track: AudioTrack?) {}
                })
            } catch (_: Exception) {
                // Ignore audio track init errors gracefully
            }
        }
    }

    fun playCoin() {
        playTone(floatArrayOf(880f, 1320f, 1760f), 120, 0.45f)
        vibrate(30)
    }

    fun playNitro() {
        playTone(floatArrayOf(300f, 450f, 600f, 850f, 1100f), 240, 0.6f)
        vibrate(80)
    }

    fun playNearMiss() {
        playTone(floatArrayOf(600f, 750f, 900f), 100, 0.4f)
        vibrate(40)
    }

    fun playCrash() {
        playTone(floatArrayOf(180f, 120f, 70f, 45f), 350, 0.8f)
        vibrate(250)
    }

    fun playShieldBreak() {
        playTone(floatArrayOf(1000f, 500f, 250f), 180, 0.5f)
        vibrate(100)
    }

    fun playClick() {
        playTone(floatArrayOf(600f), 40, 0.25f)
        vibrate(15)
    }

    fun vibrate(durationMs: Long) {
        if (!isHapticEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignore
        }
    }
}
