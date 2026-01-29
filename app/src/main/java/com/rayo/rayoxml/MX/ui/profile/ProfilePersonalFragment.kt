package com.rayo.rayoxml.mx.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.rayo.rayoxml.databinding.FragmentProfilePersonalBinding
import com.rayo.rayoxml.mx.services.User.UserRepository
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.services.User.UserViewModelFactory
import com.rayo.rayoxml.utils.PreferencesManager

class ProfilePersonalFragment : Fragment() {

    private var _binding: FragmentProfilePersonalBinding? = null
    private val binding get() = _binding!!

    // viewmodel de datos de usuario
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePersonalBinding.inflate(layoutInflater, container, false)
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
                binding.userName.text = user.nombre
                binding.userLastName.text = user.apellidos
                binding.userPhone.text = user.celular
                binding.userEmail.text = user.email
                //binding.userBirthdate.text = // No existe campo en API
            }
        }
    }
}