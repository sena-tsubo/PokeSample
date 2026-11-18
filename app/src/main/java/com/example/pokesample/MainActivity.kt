package com.example.pokesample

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {

    // ExoPlayerのインスタンスを保持する変数
    private var exoPlayer: ExoPlayer? = null
    // UriとMediaItemをクラスレベルで保持する
    private lateinit var sampleMediaItem: MediaItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //手順１---  Viewの取得 -----
        val btSamplePlay = findViewById<Button>(R.id.btSamplePlay)
        val btStop = findViewById<Button>(R.id.btStop)

        //手順２--- メディアの準備 -----
        // rawフォルダの opening.wav を指定してMediaItemを作成
        val uri = "android.resource://${packageName}/${R.raw.opening}".toUri()
        sampleMediaItem = MediaItem.fromUri(uri)

        // ---- 手順３　ExoPlayerをセットする書いてからスライド説明映る。
        // ExoPlayerの初期化とMediaItemのセット
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            // ここであらかじめセットしておく
            setMediaItem(sampleMediaItem)
            // 再生準備（バッファリングなど）をしておく
            prepare()
        }

        //手順３--- 1. 再生ボタンが押された時の処理 -----
        btSamplePlay.setOnClickListener {
            exoPlayer?.let { player ->
                // もし停止ボタンなどでシーク位置がずれていたら頭出しする
                if (player.playbackState == Player.STATE_ENDED || player.currentPosition > 0) {
                    player.seekTo(0)
                }
                player.play() // 再生開始

                // ボタンの状態を切り替え
                btSamplePlay.isEnabled = false
                btStop.isEnabled = true
            }
        }

        //手順４--- 2. 停止ボタンが押された時の処理 -----
        btStop.setOnClickListener {
            exoPlayer?.let { player ->
                player.pause() // 再生を一時停止(stopではなくpauseにして再開しやすくする)
                player.seekTo(0) // 巻き戻す

                // ボタンの状態を元に戻す
                btSamplePlay.isEnabled = true
                btStop.isEnabled = false
            }
        }

        //手順５--- 3. 再生が最後まで終わった時の処理（リスナー） -----
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                // 再生が完了 (STATE_ENDED) したらボタンの状態を戻す
                if (playbackState == Player.STATE_ENDED) {
                    btSamplePlay.isEnabled = true
                    btStop.isEnabled = false

                    // 次の再生のために頭出ししておく
                    exoPlayer?.seekTo(0)
                }
            }
        })
    }

    // アプリが終了（または画面が破棄）される時にプレーヤーを解放する!!
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}