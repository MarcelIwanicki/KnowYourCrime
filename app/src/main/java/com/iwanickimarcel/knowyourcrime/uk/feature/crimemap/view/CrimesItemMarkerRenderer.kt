package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.view

import android.content.Context
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.CrimesItemMarker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class CrimesItemMarkerRenderer(
    context: Context,
    googleMap: GoogleMap,
    clusterManager: ClusterManager<CrimesItemMarker>
) : DefaultClusterRenderer<CrimesItemMarker>(context, googleMap, clusterManager) {

    override fun onBeforeClusterItemRendered(item: CrimesItemMarker, markerOptions: MarkerOptions) {
        markerOptions.icon(item.icon)
        markerOptions.snippet(item.snippet)
        markerOptions.title(item.title)
        super.onBeforeClusterItemRendered(item, markerOptions)
    }
}