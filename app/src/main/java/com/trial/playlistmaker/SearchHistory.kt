package com.trial.playlistmaker

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

const val SEARCH_HISTORY = "search_history"
const val SEARCH_HISTORY_KEY = "search_history_key"

class SearchHistory(context: Context) {
    private val sharedPrefs = context.getSharedPreferences(SEARCH_HISTORY, AppCompatActivity.MODE_PRIVATE)
    var searchHistoryList: MutableList<Track> = mutableListOf()

    fun clearHistory() {
        sharedPrefs.edit().clear().apply()
    }

    fun getHistory(): MutableList<Track> {
        val itemsJson: String = sharedPrefs.getString(SEARCH_HISTORY_KEY, null) ?: return mutableListOf()

        return Gson().fromJson(itemsJson, object: TypeToken<MutableList<Track>>() {}.type)
    }

    private fun putTrackToHistory(tracks: MutableList<Track>?) {
        sharedPrefs.edit().putString(SEARCH_HISTORY_KEY, Gson().toJson(tracks)).apply()
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryList = getHistory()
        if (searchHistoryList.find{ it.trackId == track.trackId } != null) {
            searchHistoryList.remove(track)
        }

        searchHistoryList.add(0, track)
        putTrackToHistory(searchHistoryList)
    }
}