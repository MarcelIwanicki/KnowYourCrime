package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.repository

import com.iwanickimarcel.knowyourcrime.uk.api.CrimesRepositoryAPI
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.Crimes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CrimesRepository(private val crimesRepositoryAPI: CrimesRepositoryAPI) {

    fun getAllCrimes(latitude: Double, longitude: Double, date: String): Flow<Crimes> = flow {
        val crimes = crimesRepositoryAPI.getAllCrimes(latitude, longitude, date)
        emit(crimes)
    }.flowOn(Dispatchers.IO)

    fun getAllCrimes(poly: String, date: String): Flow<Crimes> = flow {
        val crimes = crimesRepositoryAPI.getAllCrimes(poly, date)
        emit(crimes)
    }.flowOn(Dispatchers.IO)
}