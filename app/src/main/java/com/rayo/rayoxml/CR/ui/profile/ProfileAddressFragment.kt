package com.rayo.rayoxml.CR.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.rayo.rayoxml.cr.services.User.UserRepository
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.services.User.UserViewModelFactory
import com.rayo.rayoxml.databinding.FragmentProfileAddressBinding
import com.rayo.rayoxml.utils.PreferencesManager

class ProfileAddressFragment : Fragment() {

    private var _binding: FragmentProfileAddressBinding? = null
    private val binding get() = _binding!!

    // viewmodel de datos de usuario
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileAddressBinding.inflate(layoutInflater, container, false)
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
                //Log.d("ProfileAddressFragment CR", "Datos de usuario: ${user}")
                binding.userBank.text = user.pais
                binding.userAccountType.text = user.provincia
                binding.userAccountCanton.text = user.canton
                binding.userAccountDistrito.text = user.distrito
                binding.userAccountNumber.text = user.direccionExacta
            }
        }
    }
}