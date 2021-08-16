package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class CrimesItemMarker(
    val crimeId: Int,
    lat: Double,
    lng: Double,
    val icon: BitmapDescriptor,
    private val title: String,
    private val snippet: String
) : ClusterItem {

    private val position: LatLng = LatLng(lat, lng)

    override fun getPosition(): LatLng = position

    override fun getTitle(): String = title

    override fun getSnippet(): String = snippet
}