package com.rayo.rayoxml.mx.ui.select_country

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentSelectCountryBinding
import com.rayo.rayoxml.co.models.Country
import com.rayo.rayoxml.utils.CreditInformationManager
import com.rayo.rayoxml.utils.CreditParameterManager
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.launch
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import com.rayo.rayoxml.mx.services.User.UserRepository
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.services.User.UserViewModelFactory
import com.rayo.rayoxml.mx.viewModels.AuthViewModel
import com.rayo.rayoxml.ui.main.MainActivity
import com.rayo.rayoxml.ui.main.OnLocaleChangedListener
import java.util.Locale

class SelectCountryFragment: Fragment() {
    private var _binding: FragmentSelectCountryBinding? = null
    private val binding get() = _binding!!
    private val preferencesManager by lazy { PreferencesManager(requireContext()) }

    // View model
    private lateinit var userViewModel: UserViewModel
    //private lateinit var authViewModel: AuthViewModel

    private lateinit var userViewModelCR: com.rayo.rayoxml.cr.services.User.UserViewModel
    //private lateinit var authViewModelCR: com.rayo.rayoxml.cr.viewModels.AuthViewModel

    private lateinit var userViewModelMX: com.rayo.rayoxml.mx.services.User.UserViewModel
    //private lateinit var authViewModelMX: com.rayo.rayoxml.mx.viewModels.AuthViewModel

    private var listener: OnLocaleChangedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLocaleChangedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectCountryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lista de países
        val countries = listOf(
            Country("Colombia", R.drawable.flag_colombia_svg, enabled = true),
            Country("México", R.drawable.flag_mexico_svg, enabled = false),
            Country("Costa Rica", R.drawable.flag_costa_rica_svg, enabled = true)
        )

        // Configurar RecyclerView
        val recyclerView = binding.recyclerViewCountries
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = SelectCountryAdapter(countries) { selectedCountry ->
            binding.buttonConfirm.isEnabled = true
        }
        recyclerView.adapter = adapter

        binding.buttonConfirm.isEnabled = false
        // Configurar botón de confirmación
        binding.buttonConfirm.setOnClickListener {
            val selectedCountry = adapter.getSelectedCountry()

            if (selectedCountry != null) {
                //Toast.makeText(requireContext(), "País seleccionado: ${selectedCountry.name}", Toast.LENGTH_SHORT).show()
                Log.d("SelectCountryFragment", "País: ${selectedCountry.name}")

                // Guardar país usando CreditParameterManager y CreditInformationManager
                var selectedCountryCode = "";
                when (selectedCountry.name) {
                    "México" -> selectedCountryCode = "mx"
                    "Colombia" -> selectedCountryCode = "co"
                    "Costa Rica" -> selectedCountryCode = "cr"
                    else -> print("País desconocido")
                }
                CreditParameterManager.setSelectedCountry(selectedCountryCode)
                CreditInformationManager.setSelectedCountry(selectedCountryCode)
                //Log.e("ThirdActivity", "País: $selectedCountryCode")

                // Guardar el país con DataStore (usando corutinas)
                lifecycleScope.launch {
                    // Actualizar flag para no mostrar pantalla nuevamente
                    val sharedPreferences = requireActivity().getSharedPreferences("Onboarding", Context.MODE_PRIVATE)
                    sharedPreferences.edit() {
                        putBoolean("isFirstTime", false)
                    } // O .commit() para que sea síncrono

                    preferencesManager.saveSelectedCountry(selectedCountry)
                    CreditParameterManager.setSelectedCountry(selectedCountryCode)

                    setLocaleAndRestartIfNeeded(selectedCountry.name, CreditParameterManager.getSelectedCountry())

                    Log.d("SelectCountryFragment", "País seleccionado: $selectedCountryCode")
                    // Asignar navigation graph
                    val navController = findNavController()
                    val graphInflater = navController.navInflater
                    val navGraph = when (selectedCountryCode) {
                        "co" -> graphInflater.inflate(R.navigation.main_graph_co)
                        "cr" -> graphInflater.inflate(R.navigation.main_graph_cr)
                        "mx" -> graphInflater.inflate(R.navigation.main_graph_mx)
                        else -> throw IllegalArgumentException("Invalid country")
                    }
                    navController.graph = navGraph
                    Log.d("SelectCountryFragment", "Cargando navegación para: $selectedCountryCode")

                    //navController.navigate(R.id.action_selectCountryFragment_to_loanFragment)
                    navController.navigate(R.id.loanFragment)
                }

            } else {
                //Toast.makeText(requireContext(), "Por favor, selecciona un país", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLocaleAndRestartIfNeeded(countryName: String, countryCode: String) {
        val savedLanguage = "es"

        // Retrieve last used country from SharedPreferences (or DataStore)
        val preferences = requireContext().getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastCountry = preferences.getString("LAST_COUNTRY", null)

        // Only restart if the country has actually changed
        if (lastCountry == null || lastCountry != countryName) {
            setLocale(requireContext(), savedLanguage, countryCode)

            Log.d("MainActivity", "Cambiando país a: $countryName")

            // Save new country in preferences to prevent looping
            preferences.edit().putString("LAST_COUNTRY", countryName).apply()

            /*Handler(Looper.getMainLooper()).postDelayed({
                activity?.let {
                    val intent = Intent(it, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    it.finish()
                }
            }, 200)*/
            listener?.onLocaleChanged(countryCode.uppercase())

        } else {
            Log.d("MainActivity", "País actual: $countryName")
            Log.d("MainActivity", "savedLanguage: $savedLanguage")
            Log.d("MainActivity", "countryCode: $countryCode")
            setLocale(requireContext(), savedLanguage, countryCode) // Move this here to ensure locale is updated even if no restart is needed
        }

        Log.d("LocaleCheck", "Current locale: ${resources.configuration.locales[0]}")

    }


    // Asignar país
    fun setLocale(context: Context, language: String, countryCode: String) {
        val locale = Locale(language, countryCode.uppercase())
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}