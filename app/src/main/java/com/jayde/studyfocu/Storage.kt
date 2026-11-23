String): String? = sp.getString("wp_$theme", null)
    fun setWallpaperUri(theme: String, uri: String) =
        sp.edit().putString("wp_$theme", uri).apply()
}
