package com.rayo.rayoxml.co.ui.loading

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentLoadingBinding
import com.google.android.material.navigation.NavigationView


class LoadingFragment : Fragment() {

/*
    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
         Handler(Looper.getMainLooper()).postDelayed({
             val sharedPreferences = requireActivity().getSharedPreferences("Onboarding", Context.MODE_PRIVATE)
             val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

             if (isFirstTime) {
                 findNavController().navigate(R.id.action_loadingFragment_to_viewPagerFragment)
             } else {
                 findNavController().navigate(R.id.action_loadingFragment_to_loanFragment)
             }
         }, 2000)

        _binding = FragmentLoadingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
*/

    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())

    // viewmodel datos de usuario
    //private lateinit var viewModel: UserViewModel

    //private lateinit var authViewModel: AuthViewModel
    //private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingBinding.inflate(inflater, container, false)

        /*// Compartir ViewModel con la actividad
        val repository = UserRepository()
        preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(repository, preferencesManager)
        viewModel = ViewModelProvider(requireActivity(), factory)[UserViewModel::class.java]


        val authFactory = AuthViewModelFactory(preferencesManager)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]*/

        //
        //

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Obtiene el argumento opcional con el destino de navegación
        val destinationId = arguments?.getInt("destinationId", 0) ?: 0

        // Get ViewModel
        //authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        //Handler(Looper.getMainLooper()).postDelayed({
        /*handler.postDelayed({
            if (!isAdded) return@postDelayed // Evita el error si el fragmento fue destruido
            if (destinationId != 0) {
                findNavController().navigate(destinationId)
            }
        }, 2000) // Espera 2 segundos antes de navegar*/

        // Controlar visibilidad de los mensajes
        showMessages(arguments?.getBoolean("show_messages", false) ?: false)

        // Manejar la navegación
        handler.postDelayed({
            if (!isAdded) return@postDelayed
            if (destinationId != 0) {
                findNavController().navigate(destinationId)
            }
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null) // Cancela cualquier postDelayed pendiente
    }

    private fun setSessionMenuUI(isLoggedIn: Boolean){
        // Controlar botones de login/logout
        val navigationView = requireActivity().findViewById<NavigationView>(R.id.navigation_view)
        val menu = navigationView.menu
        val logoutButton = requireActivity().findViewById<Button>(R.id.nav_logout)

        /*val navigationView = (activity as? MainActivity)?.binding?.navigationView
        navigationView?.menu?.findItem(R.id.nav_login)?.isVisible = false*/

        if(isLoggedIn){
            // Ocultar botón de login
            menu.findItem(R.id.nav_login).isVisible = false
            // Habilitar botón de logout
            logoutButton.visibility = View.VISIBLE
        }else{
            // Habilitar botón de login
            menu.findItem(R.id.nav_login).isVisible = true
            // Ocultar botón de logout
            logoutButton.visibility = View.GONE
        }
    }

    private fun showMessages(show: Boolean) {
        binding.messagesContainer?.visibility = if (show) View.VISIBLE else View.GONE

        // Ajustar margen del loader según si se muestran mensajes
        val params = binding.loadingIndicator?.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = if (show) 16 else 35 // dp (ajusta estos valores según necesites)
        binding.loadingIndicator?.layoutParams = params
    }
}