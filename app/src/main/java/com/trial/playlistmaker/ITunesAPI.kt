package com.trial.playlistmaker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesAPI {
        @GET("/search?entity=song")
        fun search(@Query("term") text: String): Call<TracksResponse>
    }
data class TracksResponse(val results: ArrayList<Track>)