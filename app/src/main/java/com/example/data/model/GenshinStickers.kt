package com.example.data.model

data class GenshinStampItem(
    val characterName: String,
    val stampEmoji: String,
    val title: String
)

object GenshinStickers {
    val items = listOf(
        GenshinStampItem("Paimon", "💫", "Emergency Food"),
        GenshinStampItem("Klee", "💥", "Fleeing Sunlight"),
        GenshinStampItem("Ganyu", "❄️", "Plenilune Gaze"),
        GenshinStampItem("Hu Tao", "👻", "Fragrance in Thaw"),
        GenshinStampItem("Nahida", "🌿", "Physic of Purity"),
        GenshinStampItem("Furina", "💧", "Soloist of Solitude"),
        GenshinStampItem("Raiden", "⚡", "Plane of Euthymia"),
        GenshinStampItem("Yae Miko", "🦊", "Astute Amusement"),
        GenshinStampItem("Venti", "🍃", "Windborne Bard"),
        GenshinStampItem("Zhongli", "🔶", "Vago Mundo"),
        GenshinStampItem("Xiao", "🌪️", "Vigilant Yaksha"),
        GenshinStampItem("Diluc", "🦅", "Darknight Hero"),
        GenshinStampItem("Ayaka", "🧊", "Frostflake Heron"),
        GenshinStampItem("Childe", "🌊", "Tartaglia"),
        GenshinStampItem("Cyno", "⚖️", "Judicator"),
        GenshinStampItem("Stray Kash", "😺", "Neko Observer"),
        GenshinStampItem("Inazuma Dog", "🐶", "Shiba Guard"),
        GenshinStampItem("Sacred Bloom", "🌸", "Sakura Tree")
    )
}
