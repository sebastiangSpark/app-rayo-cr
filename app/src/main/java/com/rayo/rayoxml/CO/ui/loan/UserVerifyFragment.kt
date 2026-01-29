package com.rayo.rayoxml.co.ui.loan

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentUserVerifyBinding
import com.rayo.rayoxml.co.services.Loan.LoanRepository
import com.rayo.rayoxml.co.services.Loan.LoanStepOneViewModel
import com.rayo.rayoxml.co.services.Loan.LoanStepOneViewModelFactory
import com.rayo.rayoxml.co.services.Loan.OTPProcessRequest
import com.rayo.rayoxml.co.services.Loan.OTPVerifyRequest
import com.rayo.rayoxml.co.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.co.ui.loan.outcome.LoanOutcome
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UserVerifyFragment : Fragment() {

    private var formId: String? = ""
    private lateinit var loanViewModel: LoanStepOneViewModel

    private var _binding: FragmentUserVerifyBinding? = null
    private val binding get() = _binding!!

    private lateinit var errorCode: TextView
    private var noCodeMessage = "Por favor, ingrese el código de verificación"
    private var wrongCodeMessage = "El código no es correcto"

    private lateinit var digitViews: List<TextView>

    private lateinit var countDownTimer: CountDownTimer

    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment

    private var attempts: Int = 0
    private var maxAttempts: Int = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        val repository = LoanRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = LoanStepOneViewModelFactory(repository, preferencesManager)
        loanViewModel = ViewModelProvider(requireActivity(), factory)[LoanStepOneViewModel::class.java]

        loanViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                formId = user.formulario
                Log.d("UserVerifyFragment", "ID form ${formId}")
            }
        }

        //loanViewModel.initVerificationAttempts()
        loanViewModel.verificationAttempts.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                attempts = data
                Log.d("UserVerifyFragment", "Intentos ${attempts}")
            }
        }

        errorCode = binding.errorCode

        // Initialize after binding is available
        digitViews = listOf(
            binding.codeDigitCero,
            binding.codeDigitOne,
            binding.codeDigitTwo,
            binding.codeDigitThree,
            binding.codeDigitFour,
            binding.codeDigitFive
        )

        // Filtro y eventos campos de codigo
        setupDigitInputs()

        // Contador código
        startTimer()

        binding.btnConfirmVerification.setOnClickListener {

            // validar intentos
            if(attempts >= maxAttempts){
                val rejectBundle = Bundle().apply {
                    putSerializable("outcome", LoanOutcome.REJECTED)
                }
                findNavController().navigate(R.id.loanOutcomeFragment, rejectBundle)
            }

            // Ocultar errores por defecto
            errorCode.visibility = View.INVISIBLE
            digitViews.forEach { digitView ->
                if (digitView.text.toString().trim().isEmpty()) {
                    digitView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
                }
            }

            val userCode = "${binding.codeDigitCero.text}${binding.codeDigitOne.text}${binding.codeDigitTwo.text}${binding.codeDigitThree.text}${binding.codeDigitFour.text}${binding.codeDigitFive.text}"
            Log.d("UserVerifyFragment", "Código: $userCode")

            // Validación de código
            val isValid = checkCode(userCode)

            if(isValid){
                loadingDialog.show(parentFragmentManager, "LoadingDialog") // Mostrar loading

                try {
                    val request = OTPVerifyRequest(
                        formulario = formId!!,
                        codigoOTP = userCode
                    )
                    lifecycleScope.launch {
                        val OtpResponse = withContext(Dispatchers.IO) {
                            repository.getDataOTPVerify(request)
                        }

                        Log.d("UserVerifyFragment", "OTP response: ${OtpResponse}")

                        if(OtpResponse != null && OtpResponse.solicitud.codigo == "200"){

                            val verificationRequest = OTPProcessRequest(
                                formulario = formId!!
                            )

                            //delay(15000)

                            val OtpProcessResponse = withContext(Dispatchers.IO) {
                                repository.getDataOTPProcess(verificationRequest)
                            }

                            loadingDialog.dismiss()

                            Log.d("UserVerifyFragment", "OTP validation response: ${OtpProcessResponse}")

                            if(OtpProcessResponse != null && OtpProcessResponse.solicitud.codigo == "200"
                                && OtpProcessResponse.solicitud.resultadoValidacion == "Valido"){
                                findNavController().navigate(
                                    R.id.action_UserVerifyFragment_to_formFragment,
                                    bundleOf("goToStep3" to true) // Enviamos un argument
                                )
                            }else{
                                // Error
                                errorCode.text = wrongCodeMessage
                                errorCode.visibility = View.VISIBLE

                                digitViews.forEach { digitView ->
                                    if (digitView.text.toString().trim().isEmpty()) {
                                        digitView.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
                                    }
                                }

                                // cargar preguntas
                                findNavController().navigate(R.id.preguntasFragment)
                            }
                        }else{
                            loadingDialog.dismiss()
                            Log.d("UserVerifyFragment", "OTP response incorrecta")
                            // cargar preguntas
                            findNavController().navigate(R.id.preguntasFragment)
                        }
                    }
                } catch (e: Exception) {
                    loadingDialog.dismiss() // Ocultar en caso de error
                    Log.e("UserVerifyFragment", "Error obteniendo datos", e)

                    val rejectBundle = Bundle().apply {
                        putSerializable("outcome", LoanOutcome.REJECTED)
                    }
                    findNavController().navigate(R.id.loanOutcomeFragment, rejectBundle)
                }
            }else{
                Log.d("UserVerifyFragment", "Código no completado")
            }

            // aumentar intentos
            loanViewModel.incrementVerificationAttempts()
        }
    }

    private fun setupDigitInputs() {

        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.isNotEmpty() && !source[0].isDigit()) "" else source
        }

        digitViews.forEach { editText ->
            editText.filters = arrayOf(inputFilter, InputFilter.LengthFilter(1))

            // Automatically move to next digit on input
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        val nextIndex = digitViews.indexOf(editText) + 1
                        if (nextIndex < digitViews.size) {
                            digitViews[nextIndex].requestFocus()
                        }
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    private fun checkCode(userCode: String): Boolean{
        val result: Boolean

        // Mensajes de error
        if(userCode.length != 6){
            errorCode.text = noCodeMessage
            errorCode.visibility = View.VISIBLE

            // Verificar cada número
            digitViews.forEach { digitView ->
                if (digitView.text.toString().trim().isEmpty()) {
                    digitView.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
                }
            }
            result = false
        }else result = true

        return result
    }

    private fun startTimer() {
        val totalTime = 60000L // 60 seconds (1 minute)
        val interval = 1000L   // Update every second

        countDownTimer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val formattedTime = String.format("00:%02d", secondsRemaining)
                binding.timer.text = formattedTime
            }

            override fun onFinish() {
                binding.timer.text = "00:00"
            }
        }

        countDownTimer.start()
    }
}