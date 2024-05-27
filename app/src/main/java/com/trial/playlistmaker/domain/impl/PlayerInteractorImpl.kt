package com.trial.playlistmaker.domain.impl

import com.trial.playlistmaker.domain.api.PlayerInteractor
import com.trial.playlistmaker.domain.api.PlayerRepository

class PlayerInteractorImpl (private val repository: PlayerRepository) : PlayerInteractor {

    override fun playbackControl() {
        repository.playbackControl()
    }

    override fun pausePlayer() {
        repository.pausePlayer()
    }

    override fun preparePlayer(previewUrl: String) {
        repository.preparePlayer(previewUrl)
    }

    override fun releasePlayer() {
        repository.releasePlayer()
    }
    override fun getState(): Int {
        return repository.getState()
    }

    override fun getCurrentPosition(): Int {
        return repository.getCurrentPosition()
    }

}