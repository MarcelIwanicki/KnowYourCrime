package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model

import com.google.android.gms.maps.model.LatLngBounds

data class CameraPosition(
    val latLngBounds: LatLngBounds,
    val latitude: Double,
    val longitude: Double
)
