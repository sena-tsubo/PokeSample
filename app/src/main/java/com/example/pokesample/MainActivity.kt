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


class MainActivity : AppCompatActivity() {

    // ExoPlayerのインスタンスを保持する変数
    private var exoPlayer: ExoPlayer? = null
    // UriとMediaItemをクラスレベルで保持する
    private val gson = Gson()

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
        runGsonMiniTest()
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
    private fun runGsonMiniTest() {
        // 1. テスト用の擬似JSONデータ（わざと不要な "moves" なども混ぜておく）[cite: 5]
        val sampleJson = """
            {
              "id": 25,
              "name": "pikachu",
              "height": 4,
              "weight": 60,
              "cries": {
                "latest": "https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/25.ogg"
              },
              "sprites": {
                "front_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
              },
              "types": [
                { "type": { "name": "electric" } }
              ],
              "moves": [
                "10まんボルト", "でんこうせっか", "ここに何千行あってもGsonは無視してくれる！"
              ]
            }
        """.trimIndent()

        // 2. ★最重要の1行：JSON文字列を PokemonResponse クラスに一発変換！[cite: 5]
        val testResult = gson.fromJson(sampleJson, PokemonResponse::class.java)

        // 3. ちゃんと箱に入ったか Logcat に出力して確認する[cite: 5]
        Log.d("GsonMiniTest", "★テスト成功！")
        Log.d("GsonMiniTest", "図鑑番号: No.${testResult.id}")
        Log.d("GsonMiniTest", "名前: ${testResult.name}")
        Log.d("GsonMiniTest", "タイプ: ${testResult.types[0].type.name}")
        Log.d("GsonMiniTest", "鳴き声URL: ${testResult.cries.latest}")
    }

    // アプリが終了（または画面が破棄）される時にプレーヤーを解放する!!
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}