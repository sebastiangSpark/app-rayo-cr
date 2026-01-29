package com.rayo.rayoxml.utils

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R

fun Fragment.navigateToRenewalFragment() {
    findNavController().navigate(R.id.action_to_renewalFragment)
}
