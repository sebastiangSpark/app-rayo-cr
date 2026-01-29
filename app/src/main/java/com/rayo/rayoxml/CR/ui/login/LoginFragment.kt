package com.rayo.rayoxml.cr.ui.login

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentLoginBinding
import com.rayo.rayoxml.cr.services.Auth.AuthRepository
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.rayo.rayoxml.cr.services.User.UserRepository
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.services.User.UserViewModelFactory
import com.rayo.rayoxml.utils.PreferencesManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    //private var _binding: FragmentLoginBinding? = null
    private lateinit var _binding: FragmentLoginBinding
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()
    // viewmodel datos de usuario
    private lateinit var viewModel: UserViewModel

    private var authToken = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<ConstraintLayout>(R.id.toolbar_login)
        val backArrow = toolbar.findViewById<ImageView>(R.id.back_toolbar_icon)
        val textRecoverPassword = view.findViewById<TextView>(R.id.recovery_password_text)

        backArrow.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_loanFragment)
        }

        textRecoverPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recoverPasswordFragment)
        }

        // Compartir ViewModel con la actividad
        val repository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(repository, preferencesManager)
        viewModel = ViewModelProvider(requireActivity(), factory)[UserViewModel::class.java]

        binding.buttonlogin.setOnClickListener {
            login()
        }
        setupPasswordToggle()
    }

    private fun goToHome() {
        lifecycleScope.launch {
            delay(300)
            view?.post {
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }

    private fun login(){
        val emailEditText: EditText = binding.root.findViewById(R.id.login_email_edit_text)
        val passwordEditText: EditText = binding.root.findViewById(R.id.login_password_edit_text)
        val errorEmailTextView = binding.root.findViewById<TextView>(R.id.error_email)
        val errorPasswordTextView = binding.root.findViewById<TextView>(R.id.error_password)

        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        var isValid = true

        // Ocultar mensajes de error previos
        errorEmailTextView.visibility = View.GONE
        errorPasswordTextView.visibility = View.GONE

        // Restablecer fondo a estado normal
        emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background)
        passwordEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background)

        // Validar email
        if (email.isEmpty()) {
            errorEmailTextView.text = "Por favor, ingrese el correo asociado a su cuenta Rayo"
            errorEmailTextView.visibility = View.VISIBLE
            emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmailTextView.text = "Correo electrónico inválido"
            errorEmailTextView.visibility = View.VISIBLE
            emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        }

        // Validar contraseña
        if (password.isEmpty()) {
            errorPasswordTextView.text = "Por favor, ingrese la contraseña"
            errorPasswordTextView.visibility = View.VISIBLE
            passwordEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        }

        if (!isValid) {
            return
        }

        lifecycleScope.launch {

            /*if(authToken == "") {
                val authResponse = authRepository.getToken()
                if (authResponse != null && authResponse.accessToken.isNotEmpty()) {
                    Log.d("LoginFragment CR", "Auth Data: ${authResponse}")
                    authToken = authResponse.accessToken
                }
            }else{
                Log.d("LoginFragment CR", "Auth Data present: ${authToken}")
            }*/

            val loginResponse = authRepository.loginUser(email, password)
            if (loginResponse != null && loginResponse.id.isNotEmpty() == true) {
                // ✅ Inicio de sesión exitoso
                emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background)
                passwordEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background)

                // Guardar datos de usuario
                viewModel.setData(loginResponse)

                // Configurar botones login/logout
                setSessionMenuUI(true)

                goToHome()
            } else {
                // ❌ Mostrar errores cuando el login es inválido
                errorEmailTextView.text = "Correo o contraseña incorrectos"
                errorEmailTextView.visibility = View.VISIBLE
                emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
                passwordEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)

                // Mostrar mensaje de error específico para la contraseña
                errorPasswordTextView.text = "Contraseña incorrecta"
                errorPasswordTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun setSessionMenuUI(isLoggedIn: Boolean){
        // Controlar botones de login/logout
        val navigationView = requireActivity().findViewById<NavigationView>(R.id.navigation_view)
        val menu = navigationView.menu
        val logoutButton = requireActivity().findViewById<Button>(R.id.nav_logout)

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

    private fun setupPasswordToggle() {
        val passwordTextInputLayout = binding.root.findViewById<TextInputLayout>(R.id.password_text_input_layout)
        passwordTextInputLayout.setEndIconOnClickListener {
            val passwordEditText = binding.root.findViewById<TextInputEditText>(R.id.login_password_edit_text)
            val isPasswordVisible = passwordEditText.inputType == InputType.TYPE_CLASS_TEXT
            passwordEditText.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT
            }
            passwordEditText.setSelection(passwordEditText.text?.length ?: 0) // Mantiene la posición del cursor
        }
    }
}