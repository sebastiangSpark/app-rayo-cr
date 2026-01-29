package com.rayo.rayoxml.cr.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.rayo.rayoxml.databinding.FragmentProfileBankBinding
import com.rayo.rayoxml.cr.services.User.UserRepository
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.services.User.UserViewModelFactory
import com.rayo.rayoxml.utils.PreferencesManager

class ProfileBankFragment : Fragment() {
    private var _binding: FragmentProfileBankBinding? = null
    private val binding get() = _binding!!

    // viewmodel de datos de usuario
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBankBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener ViewModel compartido con la actividad
        val repository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(repository, preferencesManager)
        userViewModel = ViewModelProvider(requireActivity(), factory)[UserViewModel::class.java]

        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Log.d("ProfileBankFragment CR", "Datos de banco no disponibles")
                binding.userBank.text = ""
                binding.userAccountType.text = ""
                binding.userAccountNumber.text = ""
            }
        }
    }
}