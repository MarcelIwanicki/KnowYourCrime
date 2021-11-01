package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.iwanickimarcel.knowyourcrime.uk.api.CrimesInfoService
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.CameraPosition
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.CrimesItem
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.CrimesItemMarker
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.view.CrimeMapFragmentDirections
import com.iwanickimarcel.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val DEFAULT_CAMERA_ZOOM = 16.0f

private const val FETCH_LATITUDE_OFFSET = 0.006
private const val FETCH_LONGITUDE_OFFSET = 0.006
private const val FETCH_ZOOM_OFFSET = 0.001f

private const val OFFLINE_GPS_LATITUDE = 52.21434496480181
private const val OFFLINE_GPS_LONGITUDE = 0.12568139995511415

private const val DETAILS_CAMERA_ZOOM = 22.0f
private const val DETAILS_CAMERA_ZOOM_LATITUDE_OFFSET = 0.00005443289f
private const val DETAILS_CAMERA_ZOOM_LONGITUDE_OFFSET = 0.00000268221f

class CrimeMapFragmentViewModel(
    private val crimesInfoService: CrimesInfoService
) : ViewModel() {

    companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private val _allCrimes = MutableLiveData<Crimes>()
    val allCrimes: LiveData<Crimes> = _allCrimes

    private val _currentCameraPosition = MutableLiveData<CameraPosition>()

    private val _currentGPSPosition = MutableLiveData<LatLng>()
    val currentGPSPosition: LiveData<LatLng> = _currentGPSPosition

    private val _crimeCategories = MutableLiveData<CrimeCategories>()
    val crimeCategories: LiveData<CrimeCategories> = _crimeCategories

    private val _chipCategories = MutableLiveData<MutableList<String>>()
    val chipCategories: LiveData<MutableList<String>> = _chipCategories

    private val _checkedChipsIdsList = MutableLiveData<List<Int>>()

    private val _checkedChipsNamesList = MutableLiveData<List<String>>()

    private val _dateFilteredBy = MutableLiveData<String>()
    var dateFilteredBy: LiveData<String> = _dateFilteredBy

    var resetView: Boolean = false

    fun initDateFilter() {
        viewModelScope.launch(Dispatchers.Main) {
            crimesInfoService.getLastUpdated().collect {
                if (_dateFilteredBy.value == null) {
                    _dateFilteredBy.value = it.date.substring(0, 7)
                }
            }
        }
    }

    fun handleOnClusterItemClick(
        googleMap: GoogleMap,
        crimesItemMarker: CrimesItemMarker,
        clickRedirection: (action: NavDirections) -> Unit
    ) {
        val crimesItem: CrimesItem = getCrimesItemById(crimesItemMarker.crimeId) ?: CrimesItem()
        val detailsAction =
            CrimeMapFragmentDirections.actionCrimeMapFragmentToScreenDetailsFragment(
                crimesItem
            )

        val offsetLatSub = DETAILS_CAMERA_ZOOM_LATITUDE_OFFSET
        val offsetLongSub = DETAILS_CAMERA_ZOOM_LONGITUDE_OFFSET
        val latLng = LatLng(
            crimesItem.location.latitude.toDouble() - offsetLatSub,
            crimesItem.location.longitude.toDouble() - offsetLongSub
        )

        val cameraPosition = com.google.android.gms.maps.model.CameraPosition.Builder()
            .target(latLng).zoom(DETAILS_CAMERA_ZOOM).build()

        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    googleMap.uiSettings.isScrollGesturesEnabled = true
                    clickRedirection(detailsAction)
                }

                override fun onCancel() {
                    googleMap.uiSettings.setAllGesturesEnabled(true)
                }
            })
    }

    fun loadCrimesToMapBasedOnCamera(googleMap: GoogleMap) {
        var oldPositionLat = .0
        var oldPositionLng = .0
        var oldZoom = .0f

        googleMap.setOnCameraIdleListener {
            updateCurrentCameraPosition(
                googleMap.projection.visibleRegion.latLngBounds,
                googleMap.cameraPosition.target.latitude,
                googleMap.cameraPosition.target.longitude
            )

            val distanceLat = abs(oldPositionLat - googleMap.cameraPosition.target.latitude)
            val distanceLng = abs(oldPositionLng - googleMap.cameraPosition.target.longitude)
            val differenceZoom = abs(oldZoom - googleMap.cameraPosition.zoom)

            if ((distanceLat > FETCH_LATITUDE_OFFSET || distanceLng > FETCH_LONGITUDE_OFFSET || differenceZoom > FETCH_ZOOM_OFFSET)) {
                loadAllCrimes()
                oldPositionLat = googleMap.cameraPosition.target.latitude
                oldPositionLng = googleMap.cameraPosition.target.longitude
                oldZoom = googleMap.cameraPosition.zoom
            }
        }
    }

    fun initLocationCallback(googleMap: GoogleMap) = object : LocationCallback() {
        var firstCallback = true
        override fun onLocationResult(locationResult: LocationResult) {
            if (firstCallback) {
                val location = locationResult.lastLocation
                setUpCamera(googleMap, location.latitude, location.longitude)
                firstCallback = false
            }

            if (locationResult.locations.isNotEmpty()) {
                val location = locationResult.lastLocation
                updateCurrentGPSPosition(location.latitude, location.longitude)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun initFusedLocationClient(
        googleMap: GoogleMap,
        fusedLocationClient: FusedLocationProviderClient,
        permissionRequestLauncher: ActivityResultLauncher<Array<String>>,
        locationEnabled: Boolean,
        locationPermitted: Boolean
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            updateCurrentGPSPosition(it.latitude, it.longitude)
        }
        fusedLocationClient.lastLocation.addOnCompleteListener {
            if (!locationEnabled || !locationPermitted) {
                permissionRequestLauncher.launch(PERMISSIONS)
            }
        }
        fusedLocationClient.lastLocation.addOnFailureListener {
            updateCurrentGPSPosition(OFFLINE_GPS_LATITUDE, OFFLINE_GPS_LONGITUDE)
            setUpCamera(googleMap, OFFLINE_GPS_LATITUDE, OFFLINE_GPS_LONGITUDE)
        }
    }

    fun setUpCamera(googleMap: GoogleMap, latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)

        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_CAMERA_ZOOM),
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    googleMap.uiSettings.isScrollGesturesEnabled = true
                    googleMap.uiSettings.isZoomGesturesEnabled = true
                }

                override fun onCancel() {
                    googleMap.uiSettings.setAllGesturesEnabled(true)
                }
            })
    }

    fun setUpCameraToCurrentGPSPosition(googleMap: GoogleMap) {
        val latLng = currentGPSPosition.value?.let { LatLng(it.latitude, it.longitude) }

        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng ?: LatLng(
                    OFFLINE_GPS_LATITUDE,
                    OFFLINE_GPS_LONGITUDE
                ), DEFAULT_CAMERA_ZOOM
            ),
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    googleMap.uiSettings.isScrollGesturesEnabled = true
                    googleMap.uiSettings.isZoomGesturesEnabled = true
                }

                override fun onCancel() {
                    googleMap.uiSettings.setAllGesturesEnabled(true)
                }
            })
    }

    fun updateCurrentGPSPosition(latitude: Double, longitude: Double) {
        _currentGPSPosition.value = LatLng(latitude, longitude)
    }

    private fun updateCurrentCameraPosition(
        latLngBounds: LatLngBounds,
        latitude: Double,
        longitude: Double
    ) {
        _currentCameraPosition.value = CameraPosition(
            latLngBounds,
            latitude,
            longitude
        )
    }

    fun loadCrimeCategories() {
        viewModelScope.launch {
            crimesInfoService.getRecentCrimeCategories()
                .collect {
                    _crimeCategories.value = it
                }
        }
    }

    fun loadAllCrimes() {
        var categories = _checkedChipsNamesList.value
        if (categories?.isEmpty() == true) {
            categories = null
        }

        _currentCameraPosition.value?.let { camera ->
            _currentGPSPosition.value?.let { gps ->
                categories?.let {
                    loadCrimesWithCategories(categories, camera, gps)
                } ?: run {
                    loadCrimesWithoutCategories(camera, gps)
                }
            }
        }
    }

    private fun loadCrimesWithCategories(
        categories: List<String>,
        camera: CameraPosition,
        gps: LatLng
    ) {
        viewModelScope.launch {
            if (_dateFilteredBy.value.isNullOrEmpty()) {
                crimesInfoService.getRecentCrimesWithCategoriesFromNetwork(
                    categories,
                    camera.latLngBounds,
                    gps.latitude,
                    gps.longitude,
                    camera.latitude,
                    camera.longitude
                ).collect { crime ->
                    _allCrimes.value = crime
                }
            } else {
                crimesInfoService.getCrimesWithCategoriesFromNetworkBasesOnNewDate(
                    categories,
                    camera.latLngBounds,
                    gps.latitude,
                    gps.longitude,
                    camera.latitude,
                    camera.longitude,
                    _dateFilteredBy.value!!
                ).collect { crime ->
                    _allCrimes.value = crime
                }
            }
        }
    }

    private fun loadCrimesWithoutCategories(camera: CameraPosition, gps: LatLng) {
        viewModelScope.launch {
            if (_dateFilteredBy.value.isNullOrEmpty()) {
                crimesInfoService.getAllRecentCrimesFromNetwork(
                    camera.latLngBounds,
                    gps.latitude,
                    gps.longitude,
                    camera.latitude,
                    camera.longitude
                ).collect { crime ->
                    _allCrimes.value = crime
                }
            } else {
                crimesInfoService.getAllCrimesFromNetworkWithDate(
                    camera.latLngBounds,
                    gps.latitude,
                    gps.longitude,
                    camera.latitude,
                    camera.longitude,
                    _dateFilteredBy.value!!
                ).collect { crime ->
                    _allCrimes.value = crime
                }
            }
        }

    }

    fun loadChipCategories() {
        viewModelScope.launch {
            crimesInfoService.getRecentCrimeCategories()
                .collect {
                    _chipCategories.value =
                        it.map { category ->
                            category.name
                        }.toMutableList()
                }
        }
    }

    private fun getCrimesItemById(id: Int): CrimesItem? =
        _allCrimes.value?.find {
            it.id == id
        }


    fun onSelectedChipChangesSendToViewModel(
        checkedChipIds: List<Int>,
        currentCheckedNames: MutableList<String>
    ) {
        _checkedChipsIdsList.value = checkedChipIds
        _checkedChipsNamesList.value = currentCheckedNames
    }


    fun sortListAlphabetically(isChecked: Boolean) {
        if (isChecked) {
            _allCrimes.value?.sortBy { it.category }
        } else {
            _allCrimes.value?.sortByDescending { it.category }
        }
    }

    fun sortListByDistance(isChecked: Boolean) {
        if (isChecked) {
            _allCrimes.value?.sortBy { it.distanceFromGPS }
        } else {
            _allCrimes.value?.sortByDescending { it.distanceFromGPS }
        }
    }

    fun setDataFilteredBy(data: String) {
        _dateFilteredBy.value = data
    }

    fun clearCheckedChipsNamesList() {
        val newList = mutableListOf<String>()
        _checkedChipsNamesList.value = newList
    }
}