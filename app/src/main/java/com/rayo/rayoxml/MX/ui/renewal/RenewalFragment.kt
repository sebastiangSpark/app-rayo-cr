package com.rayo.rayoxml.mx.ui.renewal

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
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.rayo.rayoxml.R
import com.rayo.rayoxml.mx.adapters.RenewalPagerAdapter
import com.rayo.rayoxml.databinding.FragmentRenewalBinding
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.viewModels.RenewalViewModel


class RenewalFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var textViewFormTitle: TextView
    private var _binding: FragmentRenewalBinding? = null
    private val binding get() = _binding!!

    var loanType: String = ""

    private var currentStep = 0
    private var totalSteps = 5 // Solo 2 pasos visibles en el stepper

    private lateinit var viewModel: UserViewModel
    // Modelo compartido
    private val renewalViewModel: RenewalViewModel by activityViewModels()

    //private var showTumipay: Boolean = false // botón informativo
    private var tumipayEnabled: Boolean = false // Habilitar desembolso

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRenewalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        textViewFormTitle = binding.textViewFormTitle
        viewPager = binding.viewPagerRenewal
        viewPager.isUserInputEnabled = false // Desactiva el swipe manual

        //if(loanType == "PLP") totalSteps = 4

        // Configurar el ViewPager y el Stepper
        /*setupViewPager()
        setupStepper()
        refreshViewPager()*/

        // Observar cambios en el ViewPager para actualizar el stepper y el título
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentStep = position
                updateStepper(position)
                updateTitle(position, loanType)
            }
        })

        // Botón de retroceso
        val backButton = requireActivity().findViewById<ImageView>(R.id.back_toolbar_icon)
        backButton.setOnClickListener {
            if (currentStep == 0) {
                // Si estamos en el paso 1, salir del fragmento
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                // Retroceder al paso anterior
                currentStep--
                viewPager.setCurrentItem(currentStep, false)
            }
        }

        // Tipo préstamo
        renewalViewModel.loanType.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanType = it
                Log.d("ProposalConfirmationFragment", "Tipo de préstamo: ${loanType}")
                // Configurar el ViewPager y el Stepper
                setupViewPager()
                setupStepper()
                refreshViewPager()
            }
        }
        // botón Tumipay
        renewalViewModel.tumipayDisbursement.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                tumipayEnabled = data.toBooleanSI()
                Log.d("RenewalFragment", "Datos opción tumipay ${data} - Mostrar? ${tumipayEnabled}")
            }
        }
    }

    fun String.toBooleanSI(): Boolean {
        return this.equals("SI", ignoreCase = true)
    }

    private fun updateTitle(step: Int, loanType: String = "") {
        if(loanType == "PLP"){
            val titles = listOf(
                "Información Personal",
                "Información General",
                "Información Laboral",
                "Propuesta del préstamo",
                "Forma de pago",
                "Confirmación de la propuesta"
            )
            textViewFormTitle.text = titles.getOrNull(step) ?: "Renovación"
        }else{
            val titles = listOf(
                "Propuesta del préstamo",
                "Forma de pago",
                "Confirmación de la propuesta"
            )
            textViewFormTitle.text = titles.getOrNull(step) ?: "Renovación"
        }
    }

    /*private fun setupViewPager() {
        val adapter = RenewalPagerAdapter(this, viewModel)
        viewPager.adapter = adapter
    }*/
    private fun setupViewPager() {
        totalSteps = if (loanType == "PLP") 6 else 3
        val adapter = RenewalPagerAdapter(this, viewModel, renewalViewModel, loanType)
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

            // Actualizar líneas
            if (view.tag?.toString()?.startsWith("line_") == true) {
                val lineIndex = view.tag.toString().split("_")[1].toInt()
                if (lineIndex < position) {
                    view.background = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_line_active)
                } else {
                    view.background = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_line_inactive)
                }
            }
        }
    }

    private fun refreshViewPager() {
        val adapter = RenewalPagerAdapter(this, viewModel, renewalViewModel, loanType)
        viewPager.adapter = adapter
        viewPager.currentItem = currentStep
    }

    fun goToNextStep() {
        Log.d("RenewalFragment", "Paso renovación: ${currentStep}")
        if (currentStep < totalSteps) {
            currentStep++
            viewPager.setCurrentItem(currentStep, false)
            updateStepper(currentStep)
        }
        else if (currentStep == totalSteps) {
            // Aquí manejas el caso en el que ya estás en el último paso (paso 3)
            Log.d("RenewalFragment", "Ya estás en el último paso: ${currentStep}")

            // Navegar al fragment de pagos
            if(loanType != "PLP"){
                if(tumipayEnabled) {
                    findNavController().navigate(R.id.tumiDisbursementFragment)
                }else {
                    findNavController().navigate(R.id.bankDisbursementFragment)
                }
            }else{
                Log.d("RenewalFragment", "último paso PLP, no se muestra desembolso")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}