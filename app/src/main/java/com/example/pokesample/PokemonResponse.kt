package com.example.pokesample

import com.google.gson.annotations.SerializedName

data class PokemonResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val cries: Cries,
    val sprites: Sprites,
    val types: List<TypeSlot> // 複数形に修正
)

data class Cries(
    val latest: String? // 鳴き声のURLが無い場合(null)に備えて String? にしておく
)

data class Sprites(
    @SerializedName("front_default") // JSONとの結び付け目印を追加
    val frontDefault: String?
)

data class TypeSlot(
    val type: TypeInfo
)

data class TypeInfo(
    val name: String // 句点を削除
)