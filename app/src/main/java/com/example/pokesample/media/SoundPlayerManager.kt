package com.example.pokecrysample.media // ← 末尾に .media がつく

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * BGMと鳴き声の再生・停止を専門に担当するクラス
 */
class SoundPlayerManager(
    context: Context,
    private val onBgmStateChanged: (Boolean) -> Unit // BGMの再生状態が変わったら画面に知らせる関数
) {
    // 1. BGM用プレイヤー（ループ再生）[cite: 5]
    private val bgmPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        repeatMode = Player.REPEAT_MODE_ONE
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onBgmStateChanged(isPlaying)
            }
        })
    }

    // 2. 鳴き声用プレイヤー（単発再生）[cite: 5]
    private val cryPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    /**
     * BGMを再生する
     */
    fun playBgm(uriString: String) {
        bgmPlayer.setMediaItem(MediaItem.fromUri(uriString))
        bgmPlayer.prepare()
        bgmPlayer.play()
    }

    /**
     * BGMを停止する
     */
    fun stopBgm() {
        bgmPlayer.stop()
    }

    /**
     * ポケモンの鳴き声を再生する[cite: 5]
     */
    fun playCry(cryUrl: String) {
        cryPlayer.setMediaItem(MediaItem.fromUri(cryUrl))
        cryPlayer.prepare()
        cryPlayer.play()
    }

    /**
     * アプリ終了時にメモリを解放する[cite: 5]
     */
    fun releaseAll() {
        bgmPlayer.release()
        cryPlayer.release()
    }
}