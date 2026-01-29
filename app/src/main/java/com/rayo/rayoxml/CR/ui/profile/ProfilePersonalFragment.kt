package com.rayo.rayoxml.cr.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.rayo.rayoxml.databinding.FragmentProfilePersonalBinding
import com.rayo.rayoxml.cr.services.User.UserRepository
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.services.User.UserViewModelFactory
import com.rayo.rayoxml.utils.PreferencesManager
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class ProfilePersonalFragment : Fragment() {

    private var _binding: FragmentProfilePersonalBinding? = null
    private val binding get() = _binding!!

    // viewmodel de datos de usuario
    private lateinit var userViewModel: UserViewModel

    private val currencySymbol = "₡"

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

        val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##0.00")

        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                //Log.d("ProfilePersonalFragment CR", "Datos de usuario: ${user}")
                binding.userName.text = user.nombre
                binding.userLastName.text = ""
                binding.userPhone.text = user.celular.takeIf { !it.isNullOrBlank() } ?: user.telefono
                binding.userEmail.text = user.email
                //binding.userBirthdate.text = // No existe campo en API
                binding.userDocument?.text = user.cedula
                binding.userWorkCountry?.text = user.lugarDeTrabajo
                binding.userSalary?.text = "$currencySymbol${formatter.format(user.salario.toDoubleOrNull())}"
                binding.userLoansNumber?.text = user.numPrestamosFirmados
            }
        }
    }
}