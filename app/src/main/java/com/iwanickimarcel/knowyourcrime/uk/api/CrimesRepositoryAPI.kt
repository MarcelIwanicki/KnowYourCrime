package com.iwanickimarcel.knowyourcrime.uk.api

import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.LastUpdated
import retrofit2.http.GET
import retrofit2.http.Query

interface CrimesRepositoryAPI {
    @GET("/api/crimes-street/all-crime")
    suspend fun getAllCrimes(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("date") date: String
    ): Crimes

    @GET("/api/crimes-street/all-crime")
    suspend fun getAllCrimes(
        @Query("poly") poly: String,
        @Query("date") date: String
    ): Crimes

    @GET("/api/crime-categories")
    suspend fun getCrimeCategories(@Query("date") date: String): CrimeCategories

    @GET("/api/crime-last-updated")
    suspend fun getLastUpdated(): LastUpdated
}