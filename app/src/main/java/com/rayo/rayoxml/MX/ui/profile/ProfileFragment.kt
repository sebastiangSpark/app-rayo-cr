package com.rayo.rayoxml.mx.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentProfileBinding
import com.rayo.rayoxml.mx.services.User.UserRepository
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.services.User.UserViewModelFactory
import com.rayo.rayoxml.mx.ui.dialogs.AvatarBottomSheetDialog
import com.rayo.rayoxml.mx.viewModels.AuthViewModel
import com.rayo.rayoxml.mx.viewModels.AuthViewModelFactory
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // viewmodel de datos de usuario
    private lateinit var userViewModel: UserViewModel

    private lateinit var authViewModel: AuthViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())

        val authFactory = AuthViewModelFactory(preferencesManager)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        val imageView = view.findViewById<ImageView>(R.id.profile_image)
        val buttonChangeImage = view.findViewById<ImageView>(R.id.edit_image)

        // Ocultar la barra de desplazamiento
        val scrollView = view.findViewById<ScrollView>(R.id.mainProfileScrollView)
        scrollView.isVerticalScrollBarEnabled = false
        scrollView.overScrollMode = View.OVER_SCROLL_NEVER

        // Observar cambios en la imagen guardada
        lifecycleScope.launch {
            authViewModel.selectedImage.collect { imageResId ->
                imageResId?.let { imageView.setImageResource(it) }
            }
        }

        buttonChangeImage.setOnClickListener {
            val bottomSheet = AvatarBottomSheetDialog { selectedImage ->
                lifecycleScope.launch {
                    authViewModel.saveSelectedImage(selectedImage)
                    updateToolbarImage(selectedImage) // También actualizar la toolbar
                }
            }
            bottomSheet.show(parentFragmentManager, "ImageSelection")
        }

        // Obtener ViewModel compartido con la actividad
        val repository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(repository, preferencesManager)
        userViewModel = ViewModelProvider(requireActivity(), factory)[UserViewModel::class.java]

        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.profileName.text = "${user.nombre} ${user.apellidos}"
            }
        }
    }

    private fun updateToolbarImage(imageResId: Int) {
        val toolbarImageView = requireActivity().findViewById<ImageView>(R.id.imgToolbarAvatar)
        toolbarImageView?.setImageResource(imageResId)
    }
}