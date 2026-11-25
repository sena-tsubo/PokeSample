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
    private var currentCryUrl: String? = null

    //【ローディング】ローディング画面(activity_main.xml の loadingOverlay)
    // ★意図: onCreate の外にある関数(fetchPokemonWithLoading)からも使うので、クラスの変数にする。
    //   画面ができるのは onCreate の中(setContentView のあと)なので、lateinit で「あとで入れる」と宣言しておく。
    private lateinit var loadingOverlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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

        val btSamplePlay = findViewById<Button>(R.id.btSamplePlay)
        val btStop = findViewById<Button>(R.id.btStop)
        val etPokemonId = findViewById<TextInputEditText>(R.id.etPokemonId)
        val btSearch = findViewById<Button>(R.id.btSearch)
        val btPlayApiCry = findViewById<Button>(R.id.btPlayApiCry)
        //【ローディング】ローディング画面を取得して、クラスの変数に入れる
        // ★意図: 中身は LinearLayout だが、表示/非表示しかしないので View として取得すれば十分
        loadingOverlay = findViewById(R.id.loadingOverlay)

        soundPlayerManager = SoundPlayerManager(this) { isPlaying ->
            btSamplePlay.isEnabled = !isPlaying
            btStop.isEnabled = isPlaying
        }
        pokeApiClient = PokeApiClient()

        val uri = "android.resource://${packageName}/${R.raw.opening}"

        btSamplePlay.setOnClickListener {
            soundPlayerManager.playBgm(uri)
        }

        btStop.setOnClickListener {
            soundPlayerManager.stopBgm()
        }

        btSearch.setOnClickListener {
            val query = etPokemonId.text.toString()
            if (query.isBlank()) return@setOnClickListener
            lifecycleScope.launch {
                //【ローディング】通信は「ローディング付き」の関数にお任せする
                // ★意図: 表示する・閉じるの細かい処理は関数の中にまとめたので、ここは1行で読める
                val pokemon = fetchPokemonWithLoading(query)
                if (pokemon != null) {
                    Log.d("MainActivity", "$pokemon")
                    val cryUrl = pokemon.cries.latest
                    currentCryUrl = if (cryUrl.isNullOrBlank()) null else cryUrl
                    Toast.makeText(this@MainActivity, R.string.success_pokemon_found, Toast.LENGTH_SHORT).show()
                    etPokemonId.text?.clear()
                } else {
                    currentCryUrl = null
                    Toast.makeText(this@MainActivity, R.string.error_pokemon_not_found, Toast.LENGTH_SHORT).show()
                }
                btPlayApiCry.isEnabled = currentCryUrl != null
            }
        }

        btPlayApiCry.setOnClickListener {
            val cryUrl = currentCryUrl ?: return@setOnClickListener
            soundPlayerManager.playCry(cryUrl)
        }
    }

    // ==========================================
    //【ローディング】ローディング画面を出しながら、ポケモンを検索する
    // ★意図: ローディングの処理(表示→通信→必ず閉じる)を、この関数の中に全部まとめる。
    //   ローディングを直したいときは、ここだけ見ればよい。
    // ★suspend を付ける理由: 中で fetchPokemon(待つ関数)を呼ぶので、この関数も「待てる関数」にする必要がある。
    // ==========================================
    private suspend fun fetchPokemonWithLoading(query: String): PokemonResponse? {
        // ① 通信を始める前に、ローディング画面を表示する
        loadingOverlay.visibility = View.VISIBLE
        try {
            // ② 通信する。結果が返るまで、ここで待つ
            return pokeApiClient.fetchPokemon(query)
        } finally {
            // ③ ★最重要: finally の中は、成功でも失敗でもエラーでも「必ず」実行される。
            //    ここで閉じれば、暗い画面のまま操作できなくなる事故が起きない。
            //    ※ return で関数を抜けるときも、抜ける直前にここが実行される。
            loadingOverlay.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayerManager.releaseAll()
    }
}
