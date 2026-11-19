package com.example.pokesample

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.load
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

// TODO: MainActivityの音源再生処理をSoundPlayerManagerに委譲（移行）する
// ※スライドでの解説ポイント：
// MainActivityにUI操作(ボタン)とビジネスロジック(音を鳴らす処理)が混ざっているので、
// 音を鳴らす処理は新しく作った SoundPlayerManager クラスに任せる（責務の分離）ことを説明する。

class MainActivity : AppCompatActivity() {

    // TODO: 以下のExoPlayer関連の変数はSoundPlayerManager内で管理するため不要になる
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

        // TODO: 手順２〜３のExoPlayer初期化・準備処理はSoundPlayerManagerに移動する
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


        // TODO: ボタンクリック時の処理の中身を SoundPlayerManager.playBgm() などの呼び出しに置き換える
        //手順３--- 1. 再生ボタンが押された時の処理 -----
        btSamplePlay.setOnClickListener {
            exoPlayer?.let { player ->
                // もし停止ボタンなどでシーク位置がずれていたら頭出しする
                if (player.playbackState == Player.STATE_ENDED || player.currentPosition > 0) {
                    player.seekTo(0)
                }
                player.play() // 再生開始

                // TODO: SoundPlayerManagerのコールバック(onBgmStateChanged)内で状態を切り替える設計に変更する
                // ボタンの状態を切り替え
                btSamplePlay.isEnabled = false
                btStop.isEnabled = true
            }
        }

        // TODO: ボタンクリック時の処理の中身を SoundPlayerManager.stopBgm() などの呼び出しに置き換える
        //手順４--- 2. 停止ボタンが押された時の処理 -----
        btStop.setOnClickListener {
            exoPlayer?.let { player ->
                player.pause() // 再生を一時停止(stopではなくpauseにして再開しやすくする)
                player.seekTo(0) // 巻き戻す

                // TODO: SoundPlayerManagerのコールバック(onBgmStateChanged)内で状態を切り替える設計に変更する
                // ボタンの状態を元に戻す
                btSamplePlay.isEnabled = true
                btStop.isEnabled = false
            }
        }

        // TODO: このListener処理はSoundPlayerManager内に実装済み(onIsPlayingChanged)のためMainActivityからは削除する
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

    // TODO: release()の直接呼び出しを、SoundPlayerManager.releaseAll() の呼び出しに変更する
    // アプリが終了（または画面が破棄）される時にプレーヤーを解放する!!
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}