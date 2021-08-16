package com.iwanickimarcel.knowyourcrime.uk.feature.settings.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iwanickimarcel.knowyourcrime.databinding.FragmentAboutUsBinding

class AboutUsFragment : Fragment() {

    private lateinit var _binding: FragmentAboutUsBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutUsBinding.inflate(inflater, container, false)

        binding.webView.loadUrl("file:///android_asset/www/index.html")
        binding.buttonBackToSettings.setOnClickListener {
            val action = AboutUsFragmentDirections.actionAboutUsFragmentToSettingsScreenFragment()
            findNavController().navigate(action)
        }
        return binding.root
    }
}