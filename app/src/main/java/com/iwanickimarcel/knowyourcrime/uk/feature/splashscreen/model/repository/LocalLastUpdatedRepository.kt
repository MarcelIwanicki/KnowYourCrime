package com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.repository

import com.iwanickimarcel.knowyourcrime.uk.api.CrimesRepositoryAPI
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.LastUpdated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LocalLastUpdatedRepository(private val crimesRepositoryAPI: CrimesRepositoryAPI) :
    LastUpdatedRepositoryAPI {
    private lateinit var lastUpdated: LastUpdated

    override fun getLastUpdated(): Flow<LastUpdated> =
        if (::lastUpdated.isInitialized) flow {
            emit(lastUpdated)
        }.flowOn(Dispatchers.IO)
        else flow {
            lastUpdated = crimesRepositoryAPI.getLastUpdated()
            emit(lastUpdated)
        }.flowOn(Dispatchers.IO)
}