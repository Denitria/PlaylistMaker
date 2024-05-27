package com.trial.playlistmaker.domain.api

interface PlayerRepository {
    fun playbackControl()
    fun pausePlayer()
    fun preparePlayer(previewUrl : String)
    fun releasePlayer()
    fun getState(): Int
    fun getCurrentPosition(): Int
}