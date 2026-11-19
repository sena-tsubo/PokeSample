package com.example.pokesample

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pokecrysample.media.SoundPlayerManager

class MainActivity : AppCompatActivity() {

    private lateinit var soundPlayerManager: SoundPlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // XMLで指定した余白(android:padding)を取り出しておく
        // ※ setPaddingだけを呼ぶとXMLの余白が上書きされて消えてしまうため、システムバーの分に「足す」
        val rootView = findViewById<View>(R.id.main)
        val basePadding = rootView.paddingLeft
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                basePadding + systemBars.left,
                basePadding + systemBars.top,
                basePadding + systemBars.right,
                basePadding + systemBars.bottom
            )
            insets
        }

        //手順１---  Viewの取得 -----
        val btSamplePlay = findViewById<Button>(R.id.btSamplePlay)
        val btStop = findViewById<Button>(R.id.btStop)
        soundPlayerManager = SoundPlayerManager(this) { isPlaying ->
            btSamplePlay.isEnabled = !isPlaying
            btStop.isEnabled = isPlaying
        }
        //手順２--- メディアの準備 -----
        // rawフォルダの opening.wav を指定してMediaItemを作成
        val uri = "android.resource://${packageName}/${R.raw.opening}"

        //手順３--- 1. 再生ボタンが押された時の処理 -----
        btSamplePlay.setOnClickListener {
            soundPlayerManager.playBgm(uri)
        }
        //手順４--- 2. 停止ボタンが押された時の処理 -----
        btStop.setOnClickListener{
            soundPlayerManager.stopBgm()
        }
    }

    // アプリが終了（または画面が破棄）される時にプレーヤーを解放する!!
    override fun onDestroy() {
        super.onDestroy()
        soundPlayerManager.releaseAll()
    }

}
