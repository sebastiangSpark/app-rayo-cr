package com.rayo.rayoxml.co.ui.select_country

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
import com.rayo.rayoxml.co.services.User.UserRepository
import com.rayo.rayoxml.co.services.User.UserViewModel
import com.rayo.rayoxml.co.services.User.UserViewModelFactory
import com.rayo.rayoxml.co.viewModels.AuthViewModel
import com.rayo.rayoxml.ui.main.MainActivity
import com.rayo.rayoxml.ui.main.OnLocaleChangedListener
import java.util.Locale

class SelectCountryFragment: Fragment() {
    private var _binding: FragmentSelectCountryBinding? = null
    private val binding get() = _binding!!
    private val preferencesManager by lazy { PreferencesManager(requireContext()) }

    private var listener: OnLocaleChangedListener? = null

    // View model
    private lateinit var userViewModel: UserViewModel
    //private lateinit var authViewModel: AuthViewModel

    private lateinit var userViewModelCR: com.rayo.rayoxml.cr.services.User.UserViewModel
    //private lateinit var authViewModelCR: com.rayo.rayoxml.cr.viewModels.AuthViewModel

    private lateinit var userViewModelMX: com.rayo.rayoxml.mx.services.User.UserViewModel
    //private lateinit var authViewModelMX: com.rayo.rayoxml.mx.viewModels.AuthViewModel

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
            Country("Colombia", R.drawable.flag_colombia_svg, enabled = false),
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

                    setLocaleAndRestartIfNeededOR(selectedCountry.name, CreditParameterManager.getSelectedCountry())

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

                    navController.navigate(R.id.loanFragment)
                }

            } else {
                //Toast.makeText(requireContext(), "Por favor, selecciona un país", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLocale(newLocale: Locale) {
        val resources = resources
        val configuration = resources.configuration
        val displayMetrics = resources.displayMetrics
        configuration.setLocale(newLocale)
        resources.updateConfiguration(configuration, displayMetrics)
    }

    private fun setLocaleAndRestartIfNeededOR(countryName: String, countryCode: String) {
        val savedLanguage = "es"

        // Retrieve last used country from SharedPreferences (or DataStore)
        val preferences = requireContext().getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastCountry = preferences.getString("LAST_COUNTRY", null)

        Log.d("SelectCountryFragment", "País actual: $countryName")
        Log.d("SelectCountryFragment", "lastCountry: $lastCountry")

        // Only restart if the country has actually changed
        if (lastCountry == null || lastCountry != countryName) {
            //setLocale(requireContext(), savedLanguage, countryCode)

            Log.d("SelectCountryFragment", "Cambiando país CO a: $countryName")

            // Save new country in preferences to prevent looping
            preferences.edit().putString("LAST_COUNTRY", countryName).apply()

            /*Handler(Looper.getMainLooper()).postDelayed({
                activity?.let {
                    Log.d("SelectCountryFragment", "Reiniciando")
                    val newLocale = Locale(countryCode.uppercase())
                    updateLocale(newLocale) // Update the locale before starting the activity

                    val intent = Intent(it, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    it.finish()
                }
            }, 200)*/
            /*Log.d("SelectCountryFragment", "Reiniciando")
            val newLocale = Locale(countryCode.uppercase())
            updateLocale(newLocale) // Update the locale before starting the activity

            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                putExtra("locale", countryCode)
            }
            startActivity(intent)*/
            listener?.onLocaleChanged(countryCode.uppercase())

        }else{
            Log.d("SelectCountryFragment", "País actual: $countryName")
            Log.d("SelectCountryFragment", "savedLanguage: $savedLanguage")
            Log.d("SelectCountryFragment", "countryCode: $countryCode")
            //setLocale(requireContext(), savedLanguage, countryCode)
        }

        // ❗ Esta parte debe ejecutarse SIEMPRE si no hay reinicio
        //setLocale(requireContext(), savedLanguage, countryCode)

        Log.d("LocaleCheck", "Current locale: ${resources.configuration.locales[0]}")

        /*// Inflate layout only after locale is set
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setContentView(R.layout.activity_main)

        /*binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)*/
        WindowCompat.setDecorFitsSystemWindows(window, false)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavViewLogin)

        bottomNavView.itemBackground = null
        val rootView = findViewById<ConstraintLayout>(R.id.rootView)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            rootView.fitsSystemWindows = true
        } else {
            rootView.fitsSystemWindows = false
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }

        initNavigation()
        setupDrawer()
        /*initUI()*/
        observeNavigation()
        hideSystemUI()
        initRemoteConfig()

        observeLoginState() // ✅ Call function to observe login state

        // Configuración del botón de retroceso en la toolbar
        findViewById<ImageView>(R.id.back_toolbar_icon).setOnClickListener {
            Log.d("ToolbarClick", "Botón de retroceso presionado")
            onBackPressedDispatcher.onBackPressed()
        }

        // Inicializar el ViewModel con un factory
        val repository = UserRepository()
        val factory = UserViewModelFactory(repository, preferencesManager)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
        try {
            loadViewData();
        }catch (e: Exception){
            println("❗ Exception: ${e.message}")
        }*/

        /*// Inicializar el ViewModel con un factory
        val repository = UserRepository()
        val factory = UserViewModelFactory(repository, preferencesManager)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        val repositoryCR = com.rayo.rayoxml.cr.services.User.UserRepository()
        val factoryCR = com.rayo.rayoxml.cr.services.User.UserViewModelFactory(repositoryCR, preferencesManager)
        userViewModelCR = ViewModelProvider(this, factoryCR)[com.rayo.rayoxml.cr.services.User.UserViewModel::class.java]

        val repositoryMX = com.rayo.rayoxml.mx.services.User.UserRepository()
        val factoryMX = com.rayo.rayoxml.mx.services.User.UserViewModelFactory(repositoryMX, preferencesManager)
        userViewModelMX = ViewModelProvider(this, factoryMX)[com.rayo.rayoxml.mx.services.User.UserViewModel::class.java]

        try {
            when (countryName) {
                "Colombia" -> Log.d("MainActivity", "Cargando datos de usuario CO")
                "Costa Rica" -> {
                    Log.d("MainActivity", "Cargando datos de usuario CR")
                    //userViewModelCR.getData(user.id)
                    //userViewModelCR.setUserFullName()
                }
                "México" -> Log.d("MainActivity", "Cargando datos de usuario MX")
                else -> throw IllegalArgumentException("País desconocido")
            }
            Log.d("MainActivity", "Cargando datos de usuario ${countryName}")

        }catch (e: Exception){
            println("❗ Exception: ${e.message}")
        }*/
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