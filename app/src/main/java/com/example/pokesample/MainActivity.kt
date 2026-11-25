package com.example.pokesample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pokecrysample.media.SoundPlayerManager
import com.example.pokesample.network.PokeApiClient
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var soundPlayerManager: SoundPlayerManager
    private lateinit var pokeApiClient: PokeApiClient

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
        val etPokemonId = findViewById<TextInputEditText>(R.id.etPokemonId)
        val btSearch = findViewById<Button>(R.id.btSearch)

        soundPlayerManager = SoundPlayerManager(this) { isPlaying ->
            btSamplePlay.isEnabled = !isPlaying
            btStop.isEnabled = isPlaying
        }
        pokeApiClient = PokeApiClient()
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
        //検索ボタンを押したときの処理
        btSearch.setOnClickListener {
            val query = etPokemonId.text.toString()
            //からなら何もしない。
            if (query.isBlank()) return@setOnClickListener
            lifecycleScope.launch {
                val pokemon = pokeApiClient.fetchPokemon(query)
                if (pokemon != null){
                    Log.d("MainActivity" , "$pokemon")
                    // 成功の合図としてToastを表示する
                    Toast.makeText(this@MainActivity, R.string.success_pokemon_found, Toast.LENGTH_SHORT).show()
                    // 入力欄を空にする
                    etPokemonId.text?.clear()
                } else {
                    //実在するポケモンじゃなかった場合には、Toastメッセージを表示する。
                    Toast.makeText(this@MainActivity, R.string.error_pokemon_not_found, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }



    // アプリが終了（または画面が破棄）される時にプレーヤーを解放する!!
    override fun onDestroy() {
        super.onDestroy()
        soundPlayerManager.releaseAll()
    }

}
