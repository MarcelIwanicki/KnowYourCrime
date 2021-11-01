package com.iwanickimarcel.knowyourcrime.uk.api

import android.location.Location
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.repository.CrimesRepository
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.LastUpdated
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.repository.CrimeCategoriesRepositoryAPI
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.repository.LastUpdatedRepositoryAPI
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

private const val LOCAL_AREA_ADDITIONAL_OFFSET = 0.006
private const val OFFLINE_LAST_UPDATED = "2021-05"

class CrimesInfoService(
    private val crimeCategoriesRepository: CrimeCategoriesRepositoryAPI,
    private val lastUpdatedRepository: LastUpdatedRepositoryAPI,
    private val crimesRepository: CrimesRepository
) {

    fun getLastUpdated(): Flow<LastUpdated> = try {
        lastUpdatedRepository.getLastUpdated()
    } catch (exception: Exception) {
        flowOf(LastUpdated(OFFLINE_LAST_UPDATED))
    }


    private fun getCrimeCategories(date: String): Flow<CrimeCategories> = try {
        crimeCategoriesRepository.getCrimeCategories(date)
    } catch (exception: Exception) {
        flowOf(CrimeCategories())
    }

    private fun getAllCrimesFromNetwork(
        latLngBounds: LatLngBounds,
        latitude: Double,
        longitude: Double,
        date: String
    ): Flow<Crimes> = flow {
        val poly = StringBuilder()
        poly.append(latitude)
            .append(",")
            .append(latLngBounds.southwest.longitude - LOCAL_AREA_ADDITIONAL_OFFSET)
            .append(":")
            .append(latLngBounds.southwest.latitude - LOCAL_AREA_ADDITIONAL_OFFSET)
            .append(",")
            .append(latLngBounds.northeast.longitude + LOCAL_AREA_ADDITIONAL_OFFSET)
            .append(":")
            .append(latLngBounds.northeast.latitude + LOCAL_AREA_ADDITIONAL_OFFSET)
            .append(",")
            .append(longitude)

        try {
            emitAll(crimesRepository.getAllCrimes(poly.toString(), date))
        } catch (exception: Exception) {
            emitAll(flowOf(Crimes()))
        }
    }.flowOn(Dispatchers.IO)

    fun getRecentCrimeCategories(): Flow<CrimeCategories> = flow {
        getLastUpdated().collect {
            emitAll(getCrimeCategories(cutDate(it.date)))
        }
    }.flowOn(Dispatchers.IO)

    fun getAllRecentCrimesFromNetwork(
        latLngBounds: LatLngBounds,
        gpsLatitude: Double,
        gpsLongitude: Double,
        latitude: Double,
        longitude: Double
    ): Flow<Crimes> = flow {
        getLastUpdated().collect {
            var crimesWithDistance = Crimes()
            getAllCrimesFromNetwork(latLngBounds, latitude, longitude, cutDate(it.date))
                .collect { crimes -> crimesWithDistance = crimes }

            repeat(crimesWithDistance.size) { i ->
                crimesWithDistance[i].distanceFromGPS = getMapDistance(
                    gpsLatitude,
                    gpsLongitude,
                    crimesWithDistance[i].location.latitude.toDouble(),
                    crimesWithDistance[i].location.longitude.toDouble()
                )
            }
            emit(crimesWithDistance)
        }
    }.flowOn(Dispatchers.IO)


    fun getAllCrimesFromNetworkWithDate(
        latLngBounds: LatLngBounds,
        gpsLatitude: Double,
        gpsLongitude: Double,
        latitude: Double,
        longitude: Double,
        date: String
    ): Flow<Crimes> = flow {
        var crimesWithDistance = Crimes()
        getAllCrimesFromNetwork(latLngBounds, latitude, longitude, date)
            .collect { crimes -> crimesWithDistance = crimes }

        repeat(crimesWithDistance.size) { i ->
            crimesWithDistance[i].distanceFromGPS = getMapDistance(
                gpsLatitude,
                gpsLongitude,
                crimesWithDistance[i].location.latitude.toDouble(),
                crimesWithDistance[i].location.longitude.toDouble()
            )
        }
        emit(crimesWithDistance)
    }.flowOn(Dispatchers.IO)

    fun getRecentCrimesWithCategoriesFromNetwork(
        categories: List<String>,
        latLngBounds: LatLngBounds,
        gpsLatitude: Double,
        gpsLongitude: Double,
        latitude: Double,
        longitude: Double
    ): Flow<Crimes> = flow {

        val newCategories = categories.toMutableList()
        repeat(newCategories.size) { i ->
            when {
                categories[i] == "Violence and sexual offences" -> {
                    newCategories[i] = "violent-crime"
                }
                categories[i] == "Criminal damage and arson" -> {
                    newCategories[i] = "criminal-damage-arson"
                }
                else -> {
                    newCategories[i] = categories[i].lowercase().replace(" ", "-")
                }
            }

        }

        getAllRecentCrimesFromNetwork(
            latLngBounds,
            gpsLatitude,
            gpsLongitude,
            latitude,
            longitude
        ).collect {
            val foundCrimes = Crimes()
            it.forEach { crime ->
                if (newCategories.contains(crime.category)) {
                    foundCrimes.add(crime)
                }
            }
            emit(foundCrimes)
        }
    }.flowOn(Dispatchers.IO)

    fun getCrimesWithCategoriesFromNetworkBasesOnNewDate(
        categories: List<String>,
        latLngBounds: LatLngBounds,
        gpsLatitude: Double,
        gpsLongitude: Double,
        latitude: Double,
        longitude: Double,
        date: String
    ): Flow<Crimes> = flow {

        val newCategories = categories.toMutableList()
        repeat(newCategories.size) { i ->
            when {
                categories[i] == "Violence and sexual offences" -> {
                    newCategories[i] = "violent-crime"
                }
                categories[i] == "Criminal damage and arson" -> {
                    newCategories[i] = "criminal-damage-arson"
                }
                else -> {
                    newCategories[i] = categories[i].lowercase().replace(" ", "-")
                }
            }
        }

        getAllCrimesFromNetworkWithDate(
            latLngBounds,
            gpsLatitude,
            gpsLongitude,
            latitude,
            longitude,
            date
        ).collect {
            val foundCrimes = Crimes()
            it.forEach { crime ->
                if (newCategories.contains(crime.category)) {
                    foundCrimes.add(crime)
                }
            }
            emit(foundCrimes)
        }
    }.flowOn(Dispatchers.IO)

    private fun cutDate(date: String): String = date.substring(0, 7)

    private fun getMapDistance(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Double {
        val result = FloatArray(1)
        Location.distanceBetween(
            startLatitude, startLongitude,
            endLatitude, endLongitude, result
        )
        return result[0].toDouble()
    }
}