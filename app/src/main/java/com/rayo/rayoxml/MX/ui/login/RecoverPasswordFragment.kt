package com.rayo.rayoxml.mx.ui.login

import android.os.Bundle
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
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentRecoverPasswordBinding
import com.rayo.rayoxml.mx.services.Auth.AuthRepository
import kotlinx.coroutines.launch


class RecoverPasswordFragment : Fragment() {

    private var _binding: FragmentRecoverPasswordBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRecoverPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<ConstraintLayout>(R.id.toolbar_recover_password)
        val backArrow = toolbar.findViewById<ImageView>(R.id.back_toolbar_icon)
        val textLoginPassword = view.findViewById<TextView>(R.id.goto_login_password_text)
        val buttonConfirm = view.findViewById<Button>(R.id.buttonConfirm)

        backArrow.setOnClickListener {
            findNavController().navigate(R.id.action_recoverPasswordFragment_to_loginFragment)
        }
        textLoginPassword.setOnClickListener {
            findNavController().navigate(R.id.action_recoverPasswordFragment_to_loginFragment)
        }

        buttonConfirm.setOnClickListener {
            recoverPassword()
        }
    }

    private fun recoverPassword(){
        val emailEditText: EditText = binding.root.findViewById(R.id.login_email_edit_text)
        val errorEmailTextView: TextView = binding.root.findViewById(R.id.error_email)
        val email = emailEditText.text.toString().trim()

        errorEmailTextView.visibility = View.GONE

        if (email.isEmpty()) {
            errorEmailTextView.text = "Por favor, ingrese el correo asociado a su cuenta Rayo"
            errorEmailTextView.visibility = View.VISIBLE
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmailTextView.text = "Correo electrónico inválido"
            errorEmailTextView.visibility = View.VISIBLE
        } else {
            lifecycleScope.launch {
                val response = authRepository.passwordRecovery(email)
                if (response != null && response.codigo == "002" ) {
                    Log.d("RecoveyPasswordFragment", "Data: ${response}")
                    Toast.makeText(requireContext(), "Correo válido, procesando recuperación", Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("RecoveyPasswordFragment", "Error: ${response}")
                    Toast.makeText(requireContext(), "Error en recuperación: ${response?.mensajeSalida}", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

}