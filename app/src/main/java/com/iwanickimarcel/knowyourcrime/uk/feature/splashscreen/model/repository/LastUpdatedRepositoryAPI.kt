package com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.repository

import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.LastUpdated
import kotlinx.coroutines.flow.Flow

interface LastUpdatedRepositoryAPI {
    fun getLastUpdated(): Flow<LastUpdated>
}