package com.trial.playlistmaker.domain.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val trackName: String,
    val artistName: String,
    @SerializedName("trackTimeMillis")
    val trackTime: String,
    var artworkUrl100: String,
    val trackId: Int,
    val collectionName: String,
    val releaseDate: String,
    val previewUrl: String,
    @SerializedName("primaryGenreName") val genre: String,
    val country: String,
) {
    fun getCoverArtwork() = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
}

