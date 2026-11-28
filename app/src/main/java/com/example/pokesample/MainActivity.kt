package com.example.pokesample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.pokecrysample.media.SoundPlayerManager
import com.example.pokesample.network.PokeApiClient
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var soundPlayerManager: SoundPlayerManager
    private lateinit var pokeApiClient: PokeApiClient
    private var currentCryUrl: String? = null
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
        loadingOverlay = findViewById(R.id.loadingOverlay)
        //【画像表示】手順１--- ポケモンの画像を表示する ImageView を取得する -----
        // ★意図: activity_main.xml に最初から用意してある ivPokemon を使う
        val ivPokemon = findViewById<ImageView>(R.id.ivPokemon)

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
                val pokemon = fetchPokemonWithLoading(query)
                if (pokemon != null) {
                    Log.d("MainActivity", "$pokemon")
                    val cryUrl = pokemon.cries.latest
                    currentCryUrl = if (cryUrl.isNullOrBlank()) null else cryUrl
                    //【画像表示】手順１--- 画像のURLを Coil に渡して表示する -----
                    // ★意図: pokemon(PokemonResponse) → sprites(Sprites) → frontDefault(画像のURL) の順にたどる。
                    //   load() は Coil というライブラリの関数。
                    //   「URLから画像をダウンロード → ImageView に表示」までを全部やってくれる。
                    ivPokemon.load(pokemon.sprites.frontDefault)
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

    private suspend fun fetchPokemonWithLoading(query: String): PokemonResponse? {
        loadingOverlay.visibility = View.VISIBLE
        try {
            return pokeApiClient.fetchPokemon(query)
        } finally {
            loadingOverlay.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayerManager.releaseAll()
    }
}
