package com.iwanickimarcel.knowyourcrime.uk.feature.detailsscreen.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.navArgs
import com.iwanickimarcel.knowyourcrime.databinding.FragmentScreenDetailsBinding
import com.iwanickimarcel.knowyourcrime.uk.feature.detailsscreen.model.ScreenDetailsCrime
import com.iwanickimarcel.knowyourcrime.uk.feature.detailsscreen.viewmodel.ScreenDetailsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject

class ScreenDetailsFragment : BottomSheetDialogFragment() {

    private val args by navArgs<ScreenDetailsFragmentArgs>()
    private val viewModel by inject<ScreenDetailsViewModel>()

    private lateinit var _binding: FragmentScreenDetailsBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScreenDetailsBinding.inflate(inflater, container, false)
        viewModel.crimesItem.observe(viewLifecycleOwner) {
            setViewsToCrimesItem(it)
        }
        return binding.root
    }

    private fun setViewsToCrimesItem(crime: ScreenDetailsCrime) {
        binding.crimeRow.textViewCategory.text = crime.category
        binding.crimeRow.textViewLocationType.text = crime.locationType
        binding.crimeRow.textViewMonth.text = crime.month
        binding.crimeRow.textViewDistance.text = crime.distance
        binding.textViewLatitude.text = crime.latitude
        binding.textViewLongitude.text = crime.longitude
        binding.textViewIdContent.text = crime.idContent
        binding.textViewWhereContent.text = crime.whereContent
        binding.crimeRow.imageViewIconBottomSheetRow.setImageResource(
            crime.iconResource
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.updateCrimesItem(args.crimeDetails)

        val bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout> =
            BottomSheetBehavior.from(binding.bottomSheetDetails)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

}