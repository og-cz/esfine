package com.example.esfine.audio

import android.content.Context
import android.media.MediaPlayer
import kotlin.random.Random

/**
 * Simple offline audio engine using Android MediaPlayer.
 * Plays two looping layers:
 *  - Texture (lofi textures)
 *  - Melody (frequency/tones)
 *
 */
class AudioEngine(private val appContext: Context) {

    private var texturePlayer: MediaPlayer? = null
    private var melodyPlayer: MediaPlayer? = null

    private var textureVol: Float = 0.6f
    private var melodyVol: Float = 0.4f

    private var lastTextureIndex: Int = -1
    private var lastMelodyIndex: Int = -1

    fun start(texturePool: IntArray, melodyPool: IntArray) {
        // Start random tracks (if not already)
        texturePlayer = replaceAndPlay(texturePlayer, texturePool, isTexture = true)
        melodyPlayer = replaceAndPlay(melodyPlayer, melodyPool, isTexture = false)
        applyVolumes()
    }

    fun stop() {
        stopAndRelease(texturePlayer)
        stopAndRelease(melodyPlayer)
        texturePlayer = null
        melodyPlayer = null
    }

    fun randomize(texturePool: IntArray, melodyPool: IntArray) {
        texturePlayer = replaceAndPlay(texturePlayer, texturePool, isTexture = true)
        melodyPlayer = replaceAndPlay(melodyPlayer, melodyPool, isTexture = false)
        applyVolumes()
    }

    fun setTextureVolume(volume: Float) {
        textureVol = volume.coerceIn(0f, 1f)
        texturePlayer?.setVolume(textureVol, textureVol)
    }

    fun setMelodyVolume(volume: Float) {
        melodyVol = volume.coerceIn(0f, 1f)
        melodyPlayer?.setVolume(melodyVol, melodyVol)
    }

    fun release() {
        stop()
    }

    private fun applyVolumes() {
        texturePlayer?.setVolume(textureVol, textureVol)
        melodyPlayer?.setVolume(melodyVol, melodyVol)
    }

    private fun replaceAndPlay(old: MediaPlayer?, pool: IntArray, isTexture: Boolean): MediaPlayer? {
        stopAndRelease(old)

        if (pool.isEmpty()) return null

        val index = pickIndexAvoidRepeat(pool.size, isTexture)
        val resId = pool[index]

        val mp = MediaPlayer.create(appContext, resId) ?: return null
        mp.isLooping = true
        mp.start()
        return mp
    }

    private fun pickIndexAvoidRepeat(size: Int, isTexture: Boolean): Int {
        if (size <= 1) return 0

        val last = if (isTexture) lastTextureIndex else lastMelodyIndex

        // Pick uniformly from the size-1 indices that are not `last`.
        // The old do/while retry could spin arbitrarily long (worst case:
        // forever) when the random pick kept landing on `last`; this is O(1).
        val idx = if (last !in 0 until size) {
            Random.nextInt(size)
        } else {
            val offset = Random.nextInt(size - 1)
            if (offset < last) offset else offset + 1
        }

        if (isTexture) lastTextureIndex = idx else lastMelodyIndex = idx
        return idx
    }

    private fun stopAndRelease(player: MediaPlayer?) {
        if (player == null) return
        try { player.stop() } catch (_: Exception) {}
        try { player.release() } catch (_: Exception) {}
    }
}
