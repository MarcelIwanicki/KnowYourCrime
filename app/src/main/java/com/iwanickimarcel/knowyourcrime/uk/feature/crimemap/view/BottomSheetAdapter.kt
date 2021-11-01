package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.iwanickimarcel.knowyourcrime.databinding.BottomSheetRowBinding
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.CrimesItem
import com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.util.IconFactory
import kotlin.math.roundToInt


class BottomSheetAdapter(
    private val dataSet: ArrayList<CrimesItem>,
    private val googleMap: GoogleMap
) : RecyclerView.Adapter<BottomSheetAdapter.ViewHolder>() {

    companion object {
        private const val LATITUDE_OFFSET = 0.00005443289f
        private const val LONGITUDE_OFFSET = 0.00000268221f
        private const val DEFAULT_CAMERA_ZOOM = 22.0f
    }

    class ViewHolder(bottomSheetRowBinding: BottomSheetRowBinding) :
        RecyclerView.ViewHolder(bottomSheetRowBinding.root) {
        lateinit var crimesItem: CrimesItem
        val category: TextView = bottomSheetRowBinding.textViewCategory
        val locationType: TextView = bottomSheetRowBinding.textViewLocationType
        val month: TextView = bottomSheetRowBinding.textViewMonth
        val gpsDistance: TextView = bottomSheetRowBinding.textViewDistance
        val icon: ImageView = bottomSheetRowBinding.imageViewIconBottomSheetRow

        lateinit var googleMap: GoogleMap
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            BottomSheetRowBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.crimesItem = dataSet[position]
        viewHolder.category.text = dataSet[position].category.replaceFirstChar {
            it.uppercase()
        }.replace('-', ' ')
        viewHolder.locationType.text = dataSet[position].location_type
        viewHolder.month.text = dataSet[position].month
        val distanceText = "${dataSet[position].distanceFromGPS.roundToInt()} m"
        viewHolder.gpsDistance.text = distanceText
        viewHolder.googleMap = googleMap
        viewHolder.icon.setImageResource(IconFactory.getIconIndexBasedOnCategoryName(dataSet[position].category))


        viewHolder.apply {
            itemView.setOnClickListener {
                val action =
                    CrimeMapFragmentDirections.actionCrimeMapFragmentToScreenDetailsFragment(
                        crimesItem
                    )
                val lng = dataSet[position].location.longitude.toDouble() - LONGITUDE_OFFSET
                val lat = dataSet[position].location.latitude.toDouble() - LATITUDE_OFFSET

                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(lat, lng)).zoom(DEFAULT_CAMERA_ZOOM).build()

                googleMap.uiSettings.isScrollGesturesEnabled = false
                googleMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    object : CancelableCallback {
                        override fun onFinish() {
                            googleMap.uiSettings.isScrollGesturesEnabled = true
                            itemView.findNavController().navigate(action)
                        }

                        override fun onCancel() {
                            googleMap.uiSettings.setAllGesturesEnabled(true)
                        }
                    })
            }
        }
    }

    override fun getItemCount() = dataSet.size
}
