package com.tecvo.taxi.utils

import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesManager @Inject constructor(
    context: Context
) {
    private val tag = "FavoritesManager"
    private val prefs: SharedPreferences = context.getSharedPreferences("taxi_favorites", Context.MODE_PRIVATE)

    // Key format: "{localAreaName}|{cityName}"
    private fun getKey(areaName: String, cityName: String): String {
        return "$areaName|$cityName"
    }

    fun addFavorite(areaName: String, cityName: String) {
        val key = getKey(areaName, cityName)
        prefs.edit().putBoolean(key, true).apply()
        Timber.tag(tag).d("Added favorite: $areaName in $cityName")
    }

    fun removeFavorite(areaName: String, cityName: String) {
        val key = getKey(areaName, cityName)
        prefs.edit().putBoolean(key, false).apply()
        Timber.tag(tag).d("Removed favorite: $areaName in $cityName")
    }

    fun toggleFavorite(areaName: String, cityName: String): Boolean {
        val isFavorite = isFavorite(areaName, cityName)
        if (isFavorite) {
            removeFavorite(areaName, cityName)
        } else {
            addFavorite(areaName, cityName)
        }
        return !isFavorite
    }

    fun isFavorite(areaName: String, cityName: String): Boolean {
        val key = getKey(areaName, cityName)
        return prefs.getBoolean(key, false)
    }

    fun getAllFavorites(): Set<String> {
        val favorites = mutableSetOf<String>()
        prefs.all.forEach { (key, value) ->
            if (value as? Boolean == true) {
                favorites.add(key)
            }
        }
        return favorites
    }
}