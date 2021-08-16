package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CrimesItem(
    val category: String = "",
    val context: String = "",
    val id: Int = 0,
    val location: Location = Location(),
    val location_subtype: String = "",
    val location_type: String = "",
    val month: String = "",
    val outcome_status: OutcomeStatus = OutcomeStatus(),
    val persistent_id: String = "",
    var distanceFromGPS: Double = .0
) : Parcelable