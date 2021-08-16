package com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.repository

import com.iwanickimarcel.knowyourcrime.uk.api.CrimesRepositoryAPI
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LocalCrimeCategoriesRepository(private val crimesRepositoryAPI: CrimesRepositoryAPI) :
    CrimeCategoriesRepositoryAPI {

    private lateinit var crimeCategories: CrimeCategories

    override fun getCrimeCategories(date: String): Flow<CrimeCategories> =
        if (::crimeCategories.isInitialized) flow {
            emit(crimeCategories)
        }.flowOn(Dispatchers.IO)
        else flow {
            crimeCategories = crimesRepositoryAPI.getCrimeCategories(date)
            emit(crimeCategories)
        }.flowOn(Dispatchers.IO)

}