package com.example.pokesample.network

import android.util.Log
import com.example.pokesample.PokemonResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class PokeApiClient {
    private val httpClient = OkHttpClient()
    private val gson = Gson()

    suspend fun fetchPokemon(query: String) : PokemonResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://pokeapi.co/api/v2/pokemon/$query"
                val request = Request.Builder().url(url).build()
                httpClient.newCall(request).execute().use { response ->
                    val result = response.body?.string() ?: return@withContext null
                    //GsonのJSON文字列をPokemonResponseクラスに一発変換！
                    gson.fromJson(result, PokemonResponse::class.java)
                }
            } catch (e: Exception){
                Log.e("PokeApiClient" , "通信エラー");
                null
            } as PokemonResponse?
        }
    }
}