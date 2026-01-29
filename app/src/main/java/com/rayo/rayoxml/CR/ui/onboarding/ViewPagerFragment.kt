package com.rayo.rayoxml.cr.ui.onboarding

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentViewPagerBinding
import com.rayo.rayoxml.cr.ui.onboarding.screens.FirstScreen
import com.rayo.rayoxml.cr.ui.onboarding.screens.FourthScreen
import com.rayo.rayoxml.cr.ui.onboarding.screens.SecondScreen
import com.rayo.rayoxml.cr.ui.onboarding.screens.ThirdScreen

class ViewPagerFragment : Fragment() {

    private var _binding: FragmentViewPagerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentsList = listOf(
            FirstScreen(::goToNextPage, ::goToHome),
            SecondScreen(::goToNextPage, ::goToHome),
            ThirdScreen(::goToNextPage, ::goToHome),
            FourthScreen(::goToHome)
        )

        val adapter = ViewPagerAdapter(this, fragmentsList)
        binding.viewPager.adapter = adapter

        binding.dotsIndicator.attachTo(binding.viewPager)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == binding.viewPager.adapter?.itemCount?.minus(1)) {
                    // Mostrar el botón "Continuar" en la última página
                    binding.buttonNext.visibility = View.GONE
                    binding.btnSkip.visibility = View.GONE
                    binding.continueButton.visibility = View.VISIBLE
                } else {
                    binding.buttonNext.visibility = View.VISIBLE
                    binding.btnSkip.visibility = View.VISIBLE
                    binding.continueButton.visibility = View.GONE
                }
            }
        })

        binding.buttonNext.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < binding.viewPager.adapter?.itemCount ?: 0) {
                binding.viewPager.currentItem = nextItem
            } else {
                goToHome()
            }
        }

        binding.btnSkip.setOnClickListener {
            goToHome()
        }

        binding.continueButton.setOnClickListener {
            goToHome()
        }

        binding.continueButton.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_selectCountryFragment)
        }
    }

    private fun goToNextPage() {
        binding.viewPager.currentItem = binding.viewPager.currentItem + 1
    }

    private fun goToHome() {
        binding.viewPager.currentItem = (binding.viewPager.adapter?.itemCount ?: 1) - 1
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun Int.dpToPx(): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (this * density).toInt()
    }
}
