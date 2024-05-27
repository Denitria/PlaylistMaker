package com.trial.playlistmaker

import com.trial.playlistmaker.data.repository.PlayerRepositoryImpl
import com.trial.playlistmaker.domain.api.PlayerInteractor
import com.trial.playlistmaker.domain.impl.PlayerInteractorImpl
import com.trial.playlistmaker.domain.api.PlayerRepository

object Creator {

    private fun getPlayerRepository(): PlayerRepository {
        return PlayerRepositoryImpl()
    }

    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl(getPlayerRepository())
    }
}
