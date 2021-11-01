package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.maps.android.clustering.ClusterManager
import com.iwanickimarcel.knowyourcrime.R
import com.iwanickimarcel.knowyourcrime.databinding.CrimeMapBinding
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.CrimesItem
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.CrimesItemMarker
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import kotlinx.coroutines.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

private const val GPS_FETCH_TIMEOUT = 1000L
private const val GPS_FETCH_INTERVAL = 100L

class CrimeMapFragment : Fragment() {

    private val viewModel by sharedViewModel<CrimeMapFragmentViewModel>()
    private lateinit var _binding: CrimeMapBinding
    private val binding get() = _binding

    private lateinit var googleMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<CrimesItemMarker>

    private lateinit var bottomSheetAdapter: BottomSheetAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var gpsMarker: Marker? = null

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.all {
                it.value == true
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CrimeMapBinding.inflate(inflater, container, false)

        viewModel.initDateFilter()

        viewModel.allCrimes.observe(viewLifecycleOwner) {
            setMarkersForCrimes(it)
            setBottomListForCrimes(it)
        }

        viewModel.chipCategories.observe(viewLifecycleOwner) {
            setChipsForChipCategories(it, container)
        }

        viewModel.currentGPSPosition.observe(viewLifecycleOwner) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                withTimeout(GPS_FETCH_TIMEOUT) {
                    setGPSMarker(it)
                }
            }
        }

        loadViewModelData()
        loadGoogleMaps()
        loadBottomSheet()
        loadGPS()
        loadSettings()

        return binding.root
    }

    private fun loadSettings() {
        binding.fabSettings.setOnClickListener {
            val action =
                CrimeMapFragmentDirections.actionCrimeMapFragmentToSettingsScreenFragment()
            viewModel.clearCheckedChipsNamesList()
            findNavController().navigate(action)
        }
    }

    private fun loadGPS() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationRequest = LocationRequest.create().apply {
            interval = GPS_FETCH_INTERVAL
            fastestInterval = GPS_FETCH_INTERVAL / 2
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = GPS_FETCH_INTERVAL
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            if (::googleMap.isInitialized) {
                locationCallback = viewModel.initLocationCallback(googleMap)
            }
        }

    }

    private fun setGPSMarker(latLng: LatLng) {
        if (gpsMarker != null) {
            gpsMarker?.remove()
        }
        gpsMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(getString(R.string.you_are_here))
        )
    }

    private fun setBottomListForCrimes(crimes: Crimes) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            binding.bottomSheet.recyclerViewBottomSheet.apply {
                layoutManager = LinearLayoutManager(activity)
                bottomSheetAdapter =
                    BottomSheetAdapter(crimes as ArrayList<CrimesItem>, googleMap)
                adapter = bottomSheetAdapter
            }
        }
    }

    private fun setMarkersForCrimes(crimes: Crimes) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                clusterManager.clearItems()
            }
            val icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_image)
            crimes.forEach { crime ->
                withContext(Dispatchers.Main) {
                    clusterManager.addItem(
                        CrimesItemMarker(
                            crime.id,
                            crime.location.latitude.toDouble(),
                            crime.location.longitude.toDouble(),
                            icon,
                            crime.category,
                            crime.location.street.name
                        )
                    )
                }
            }
            withContext(Dispatchers.Main) {
                clusterManager.cluster()
            }
        }
    }

    private fun setChipsForChipCategories(
        chipCategories: MutableList<String>,
        container: ViewGroup?
    ) {
        val chipsList = mutableListOf<Chip>()
        addChipsToViewAndLoadChipsList(chipCategories, container, chipsList)
        setChipsListenerAndUpdateViewModel(chipsList)
    }

    private fun addChipsToViewAndLoadChipsList(
        categoryList: List<String>,
        container: ViewGroup?,
        chipsList: MutableList<Chip>
    ) {
        if (viewModel.resetView) {
            binding.chipGroup.removeAllViews()
        }
        categoryList.forEach { categoryName ->
            val chip = layoutInflater.inflate(R.layout.chip_item, container, false) as Chip
            chip.text = categoryName
            chip.chipIcon = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_sports_baseball_24
            )
            chip.id = View.generateViewId()
            chipsList.add(chip)
            binding.chipGroup.addView(chip)
        }
    }

    private fun setChipsListenerAndUpdateViewModel(chipsList: MutableList<Chip>) {
        chipsList.forEach { chip ->
            chip.setOnClickListener {
                val currentCheckedNames = mutableListOf<String>()
                binding.chipGroup.checkedChipIds.forEach { chipId ->
                    currentCheckedNames.add(binding.chipGroup.findViewById<Chip>(chipId).text.toString())
                }
                viewModel.onSelectedChipChangesSendToViewModel(
                    binding.chipGroup.checkedChipIds,
                    currentCheckedNames
                )
                viewModel.loadAllCrimes()
            }
        }
    }

    private fun loadViewModelData() {
        viewModel.loadCrimeCategories()
        viewModel.loadChipCategories()
    }

    private fun loadGoogleMaps() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { handleGoogleMapLoaded(it) }
    }

    private fun handleGoogleMapLoaded(map: GoogleMap) {
        googleMap = map
        initClusterManager()
        viewModel.loadCrimesToMapBasedOnCamera(googleMap)
        viewModel.initFusedLocationClient(
            googleMap,
            fusedLocationClient,
            permissionRequestLauncher,
            isLocationEnabled(),
            checkLocationPermission()
        )
    }

    private fun loadBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.bottomSheet)

        binding.fab.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            else
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.fabCurrentPosition.setOnClickListener {
            viewModel.setUpCameraToCurrentGPSPosition(googleMap)
        }

        binding.bottomSheet.sortByDistance.setOnClickListener {
            viewModel.sortListByDistance(binding.bottomSheet.sortByDistance.isChecked)

            bottomSheetAdapter.notifyItemChanged(0)
            binding.bottomSheet.recyclerViewBottomSheet.smoothScrollToPosition(0)
        }

        binding.bottomSheet.sortAlphabetically.setOnClickListener {
            viewModel.sortListAlphabetically(binding.bottomSheet.sortAlphabetically.isChecked)

            bottomSheetAdapter.notifyItemChanged(0)
            binding.bottomSheet.recyclerViewBottomSheet.smoothScrollToPosition(0)
        }
    }

    private fun initClusterManager() {
        clusterManager = ClusterManager(requireActivity(), googleMap)
        googleMap.setOnCameraIdleListener(clusterManager)

        clusterManager.renderer = CrimesItemMarkerRenderer(
            requireActivity(),
            googleMap,
            clusterManager
        )

        clusterManager.setOnClusterItemClickListener {
            viewModel.handleOnClusterItemClick(googleMap, it) { action ->
                view?.findNavController()?.navigate(action)
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            if (::locationCallback.isInitialized) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            ||
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
}