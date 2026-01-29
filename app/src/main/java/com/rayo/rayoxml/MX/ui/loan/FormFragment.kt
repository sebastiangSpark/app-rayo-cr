package com.rayo.rayoxml.mx.ui.loan

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.rayo.rayoxml.R
import com.rayo.rayoxml.mx.adapters.FormPagerAdapter
import com.rayo.rayoxml.databinding.FragmentFormBinding
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.viewModels.AuthViewModel
import com.rayo.rayoxml.mx.viewModels.AuthViewModelFactory
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.mx.viewModels.RenewalViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FormFragment : Fragment(), BankInfoNavigationListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var textViewFormTitle: TextView
    private var shouldUpdateStepper = true
    private var _binding: FragmentFormBinding? = null
    private val binding get() = _binding!!

    private var currentStep = 0
    private val totalSteps = 4

    // Datos de formulario
    private var formId: String? = null
    private var stepOneFormId: String = ""
    private var stepTwoFormId: String = ""
    private var proposalValue: String = "0"

    // Compartir parámetros
    private lateinit var viewModel: UserViewModel
    //private var showTumipay: Boolean = false // botón informativo
    private var tumipayEnabled: Boolean = false // Habilitar desembolso

    // Modelo compartido
    private val renewalViewModel: RenewalViewModel by activityViewModels()

    private lateinit var authViewModel: AuthViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textViewFormTitle = binding.textViewFormTitle
        viewPager = binding.viewPager
        viewPager.isUserInputEnabled = false // Desactiva el swipe manual

        preferencesManager = PreferencesManager(requireContext())

        val factory = AuthViewModelFactory(preferencesManager)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setupViewPager()
        setupStepper()
        refreshViewPager()

        viewModel.formId.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("FormFragment", "Formulario asignado: $data")
            }
        }
        /*viewModel.showTumiPay.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("FormFragment", "Opción TumiPay asignado: $data")
                showTumipay = data
            }
        }*/
        // botón Tumipay
        renewalViewModel.tumipayDisbursement.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                tumipayEnabled = data.toBooleanSI()
                Log.d("RenewalFragment", "Datos opción tumipay ${data} - Mostrar? ${tumipayEnabled}")
            }
        }

        // Recibir los datos del argumento
        val bundle = Bundle().apply {
            putString("via", arguments?.getString("via"))
            putString("number", arguments?.getString("number"))
            putString("carrer", arguments?.getString("carrer"))
            putString("complement", arguments?.getString("complement"))
            putString("detail", arguments?.getString("detail"))
        }
        val adapter = FormPagerAdapter(this, viewModel, bundle)
        viewPager.adapter = adapter
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = FormPagerAdapter(this, viewModel, bundle)

        val shouldNavigateToStep2 = arguments?.getBoolean("goToStep2", false) ?: false
        if (shouldNavigateToStep2) {
            currentStep = 1
            viewPager.setCurrentItem(currentStep, false)
            updateStepper(currentStep)
        }

        val shouldNavigateToStep3 = arguments?.getBoolean("goToStep3", false) ?: false

        if (shouldNavigateToStep3) {
            currentStep = 2
            viewPager.setCurrentItem(currentStep, false)
            updateStepper(currentStep)
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentStep = position
                if (shouldUpdateStepper) {
                    updateStepper(position)
                }
                updateTitle(position)

                val openDialogIcon = requireActivity().findViewById<ImageView>(R.id.toolbar_layout_icon)
                when (position) {
                    0 -> openDialogIcon.visibility = View.GONE
                    1 -> openDialogIcon.visibility = View.VISIBLE
                    else -> openDialogIcon.visibility = View.GONE
                }
            }
        })

        val backButton = requireActivity().findViewById<ImageView>(R.id.back_toolbar_icon)
        backButton.setOnClickListener {
            Log.d("ToolbarClick", "Botón de retroceso presionado")
            if (currentStep == 0) {
                // Si estamos en el step 1, navegar al LoanFragment
                showBackDialog()
                /*navigateToLoanFragment()*/
            } else {
                // Si estamos en otro step, retroceder al paso anterior
                if (viewPager.currentItem > 0) {
                    currentStep = viewPager.currentItem - 1
                    viewPager.setCurrentItem(currentStep, false)
                    Log.d("ToolbarClick", "Retrocediendo a $currentStep")
                } else {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    Log.d("ToolbarClick", "Saliendo del formulario")
                }
            }
        }
        val openDialogIcon = requireActivity().findViewById<ImageView>(R.id.toolbar_layout_icon)

        openDialogIcon.setOnClickListener {
            showTermsAndConditionsDialog()
        }
    }

    private fun showTermsAndConditionsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_bank_info, null)

        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogView)
            // Hace que el fondo del diálogo sea transparente para respetar las esquinas redondeadas
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogView.findViewById<TextView>(R.id.btn_submit_bank).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showBackDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_back_bank_info, null)

        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogView)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogView.findViewById<TextView>(R.id.btn_back_bank).setOnClickListener {
            navigateToLoanFragment()
            dialog.dismiss()
        }
        dialogView.findViewById<TextView>(R.id.btn_submit_bank).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun String.toBooleanSI(): Boolean {
        return this.equals("SI", ignoreCase = true)
    }

    private fun updateTitle(step: Int) {
        val titles = listOf(
            "Información personal",
            "Información Bancaria",
            "Propuesta del Préstamo",
            "Confirmación de la Propuesta",
            "Confirmación de la Propuesta"
        )
        textViewFormTitle.text = titles.getOrNull(step) ?: "Formulario"
    }

    override fun onNavigateToAddressInfo() {
        findNavController().navigate(R.id.action_formFragment_to_addressInfoFormFragment)
    }


    private fun setupViewPager() {
        val adapter = FormPagerAdapter(this, viewModel)
        viewPager.adapter = adapter
    }

    private fun setupStepper() {
        for (i in 0 until totalSteps) {
            val circle = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(50, 50).apply {
                    setMargins(0, 0, 0, 0)
                }
                background = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_inactive)
                tag = "circle_$i"
            }
            binding.stepperLayout.addView(circle)

            if (i < totalSteps - 1) {
                val line = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(47, 3).apply {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_line_inactive)
                    tag = "line_$i"
                }
                binding.stepperLayout.addView(line)
            }
        }
        updateStepper(0)
    }

    private fun updateStepper(position: Int) {
        for (i in 0 until binding.stepperLayout.childCount) {
            val view = binding.stepperLayout.getChildAt(i)

            // Actualizar círculos
            if (view.tag?.toString()?.startsWith("circle_") == true) {
                val stepIndex = view.tag.toString().split("_")[1].toInt()
                when {
                    stepIndex < position -> {
                        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_completed)
                    }
                    stepIndex == position -> {
                        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_active)
                    }
                    else -> {
                        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_inactive)
                    }
                }
            }

            if (view.tag?.toString()?.startsWith("line_") == true) {
                val lineIndex = view.tag.toString().split("_")[1].toInt()
                if (lineIndex < position) {
                    view.background = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_line_active)
                } else {
                    view.background = ContextCompat.getDrawable(requireContext(), R.drawable.lines_steppers)
                }
            }
        }
    }


    private fun navigateToLoanFragment() {
        val navController = findNavController()
        navController.navigate(R.id.loanFragment)
    }


    fun goToNextStep(bundle: Bundle? = null, skipStep: Boolean = false, shouldUpdateStepper: Boolean = true) {
        Log.d("FormFragment", "Step: ${currentStep}")
        this.shouldUpdateStepper = shouldUpdateStepper
        if (currentStep < 4) {
            // Validar si se salta paso (valaidación OTP)
            if(skipStep) currentStep += 2
            else currentStep++

            // Destruye el fragmento actual y recarga el ViewPager2
            viewPager.adapter = FormPagerAdapter(this, viewModel)
            viewPager.setCurrentItem(currentStep, false)

            if (shouldUpdateStepper) {
                updateStepper(currentStep)
            }

            val option = bundle?.getString("option", "")
            when (option) {
                "stepOneFormId" -> {
                    stepOneFormId = bundle.getString("formId", "").toString()
                }
                "stepTwoFormId" -> {
                    stepTwoFormId = bundle.getString("formId", "").toString()
                }
                "proposal" -> {
                    proposalValue = bundle.getString("proposalValue", "").toString()
                }
            }

            formId = stepOneFormId

            Log.d("FormFragment", "Opción: ${option}")
            Log.d("FormFragment", "Id formulario: ${formId}")
            if(proposalValue !== "0"){
                Log.d("FormFragment", "Valor de la propuesta: ${proposalValue}")
                //findNavController().navigate(R.id.action_bankInfoFormFragment_to_loanProposalFragment)
            }
        }

        else if (currentStep == 4) {
            // Aquí manejas el caso en el que ya estás en el último paso (paso 3)
            Log.d("FormFragment", "Ya estás en el último paso: ${currentStep}")

            // Navegar al fragment de pagos
            if(tumipayEnabled) {
                findNavController().navigate(R.id.tumiDisbursementFragment)
            }else {
                findNavController().navigate(R.id.bankDisbursementFragment)
            }
        }
    }

    private fun navigateToHomeOrLoanFragment() {
        // Redireccionar a pantalla siguiente
        viewLifecycleOwner.lifecycleScope.launch {
            //val preferencesManager = PreferencesManager(requireContext())
            val isLoggedIn = authViewModel.isLoggedIn.firstOrNull() ?: false

            val destination = if (isLoggedIn) {
                R.id.homeFragment
            } else {
                R.id.loginFragment
            }

            if (findNavController().currentDestination?.id != destination) {
                findNavController().navigate(destination)
            }
        }
    }

    private fun navigateToPayMethod() {
        findNavController().navigate(R.id.bankDisbursementFragment)
    }

    fun getProposalValue(): String {
        return proposalValue
    }

    fun navigateToUserVerifyFragment() {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.formFragment) {
            navController.navigate(R.id.action_formFragment_to_UserVerifyFragment)
        }
    }

    private fun refreshViewPager() {
        val adapter = FormPagerAdapter(this, viewModel)
        viewPager.adapter = adapter
        viewPager.currentItem = currentStep
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}