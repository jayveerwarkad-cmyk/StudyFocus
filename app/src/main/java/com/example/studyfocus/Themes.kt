package com.example.studyfocus

import android.content.Context

data class AppTheme(
    val id: String,
    val name: String,
    val bgColor: String,
    val textColor: String,
    val accentColor: String,
    val imageName: String?,   // res/drawable में तस्वीर का नाम (बिना .jpg)
    val live: String?         // stars, leaves, rain, sparks
)

object Themes {
    val all = listOf(
        AppTheme("naruto", "Naruto", "#E65100", "#FFFFFF", "#1A237E", "bg_naruto", "leaves"),
        AppTheme("onepiece", "One Piece", "#01579B", "#FFFFFF", "#FFC107", "bg_onepiece", "stars"),
        AppTheme("dragonball", "Dragon Ball", "#F57C00", "#FFFFFF", "#0D47A1", "bg_dragonball", "sparks"),
        AppTheme("jjk", "Jujutsu Kaisen", "#1A1A2E", "#FFFFFF", "#7C4DFF", "bg_jjk", "stars"),
        AppTheme("chainsaw", "Chainsaw Man", "#B71C1C", "#FFFFFF", "#212121", "bg_chainsaw", "sparks"),
        AppTheme("mha", "My Hero Academia", "#2E7D32", "#FFFFFF", "#FDD835", "bg_mha", "sparks"),
        AppTheme("demonslayer", "Demon Slayer", "#1B5E20", "#FFFFFF", "#E91E63", "bg_demonslayer", "leaves"),
        AppTheme("aot", "Attack on Titan", "#3E2723", "#FFFFFF", "#827717", "bg_aot", "rain"),
        AppTheme("deathnote", "Death Note", "#000000", "#FFFFFF", "#D50000", "bg_deathnote", "rain"),
        AppTheme("bleach", "Bleach", "#263238", "#FFFFFF", "#FF6F00", "bg_bleach", "sparks"),
        AppTheme("hxh", "Hunter x Hunter", "#33691E", "#FFFFFF", "#FFEB3B", "bg_hxh", "leaves"),
        AppTheme("tokyoghoul", "Tokyo Ghoul", "#212121", "#FFFFFF", "#C62828", "bg_tokyoghoul", "rain"),
        AppTheme("tokyorev", "Tokyo Revengers", "#0D1B2A", "#FFFFFF", "#FFB300", "bg_tokyorev", "sparks"),
        AppTheme("space", "Space / Universe", "#050A1F", "#FFFFFF", "#7C4DFF", null, "stars"),
        AppTheme("moon", "Moon Night", "#0B132B", "#E0E0E0", "#90CAF9", null, "stars"),
        AppTheme("forest", "Green Forest", "#1B5E20", "#FFFFFF", "#A5D6A7", null, "leaves"),
        AppTheme("rain", "Rainy Day", "#263238", "#FFFFFF", "#4FC3F7", null, "rain")
    )

    private fun sp(c: Context) = c.getSharedPreferences("study_focus", Context.MODE_PRIVATE)

    fun saveChoice(c: Context, id: String) = sp(c).edit().putString("theme", id).apply()

    fun current(c: Context): AppTheme {
        val id = sp(c).getString("theme", "space")
        return all.firstOrNull { it.id == id } ?: all.first { it.id == "space" }
    }
}
