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

    //【鳴き声】手順１--- 検索で見つかったポケモンの鳴き声URLを覚えておく入れ物 -----
    // 鳴き声ボタンを押すのは、検索が終わった「あと」になる。
    // launchの中の変数(pokemon)は、その { } の外では使えなくなってしまうので、
    // Activityのプロパティ(クラスの変数)に保存しておく。
    // null = まだ検索していない、または検索に失敗した(＝再生できる鳴き声がない)
    private var currentCryUrl: String? = null

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
        //【鳴き声】手順２--- 鳴き声ボタンを取得する -----
        // XMLで最初は無効(android:enabled="false")にしてあるので、検索に成功するまで押せない
        val btPlayApiCry = findViewById<Button>(R.id.btPlayApiCry)

        soundPlayerManager = SoundPlayerManager(this) { isPlaying ->
            btSamplePlay.isEnabled = !isPlaying
            btStop.isEnabled = isPlaying
        }
        pokeApiClient = PokeApiClient()

        // rawフォルダの opening.wav を指定してMediaItemを作成
        val uri = "android.resource://${packageName}/${R.raw.opening}"


        btSamplePlay.setOnClickListener {
            soundPlayerManager.playBgm(uri)
        }

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
                    //【鳴き声】手順１--- 取得したポケモンの鳴き声URLを、入れ物に保存する -----
                    // pokemon(PokemonResponse) → cries(Cries) → latest(String?) の順にたどる
                    // (JSONにURLが無い場合に備えて、型は String? にしてある)
                    val cryUrl = pokemon.cries.latest
                    //【鳴き声】手順４--- 鳴き声がない(nullまたは空)ポケモンは、null を保存する -----
                    // こうしておくと currentCryUrl は「使えるURL」か「null」のどちらかだけになる。
                    // → 手順２(ボタンの有効/無効)も、手順３(再生)も、今のコードのまま安全に動く。
                    currentCryUrl = if (cryUrl.isNullOrBlank()) null else cryUrl
                    // 成功の合図としてToastを表示する
                    Toast.makeText(this@MainActivity, R.string.success_pokemon_found, Toast.LENGTH_SHORT).show()
                    // 入力欄を空にする
                    etPokemonId.text?.clear()
                } else {
                    //【鳴き声】手順１--- 検索に失敗したら、前のポケモンの鳴き声URLを消しておく -----
                    // (消さないと、あとで間違ったポケモンの鳴き声を再生してしまう)
                    currentCryUrl = null
                    //実在するポケモンじゃなかった場合には、Toastメッセージを表示する。
                    Toast.makeText(this@MainActivity, R.string.error_pokemon_not_found, Toast.LENGTH_SHORT).show()
                }
                //【鳴き声】手順２--- 鳴き声ボタンを押せるかどうかは、「URLがあるかどうか」で決める -----
                // 成功 → currentCryUrl にURLが入っている → true(押せる)
                // 失敗 → currentCryUrl は null → false(押せない)
                btPlayApiCry.isEnabled = currentCryUrl != null
            }
        }

        //【鳴き声】手順３--- 鳴き声ボタンが押された時の処理 -----
        btPlayApiCry.setOnClickListener {
            // 入れ物にURLが入っていない(null)場合は何もしない
            // (ボタンは無効なので通常はここに来ないが、念のためのガード)
            val cryUrl = currentCryUrl ?: return@setOnClickListener
            // 鳴き声の再生は、SoundPlayerManagerにお願いする
            soundPlayerManager.playCry(cryUrl)
        }

    }



    // アプリが終了（または画面が破棄）される時にプレーヤーを解放する!!
    override fun onDestroy() {
        super.onDestroy()
        soundPlayerManager.releaseAll()
    }

}
