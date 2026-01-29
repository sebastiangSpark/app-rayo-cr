package com.rayo.rayoxml.ui.main

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.ActivityMainBinding
import com.rayo.rayoxml.co.models.Country
import com.rayo.rayoxml.co.services.User.UserRepository
import com.rayo.rayoxml.co.services.User.UserViewModel
import com.rayo.rayoxml.co.services.User.UserViewModelFactory
import com.rayo.rayoxml.co.viewModels.AuthViewModel
import com.rayo.rayoxml.co.viewModels.AuthViewModelFactory
import com.rayo.rayoxml.utils.CreditInformationManager
import com.rayo.rayoxml.utils.CreditParameterManager
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.co.viewModels.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.Locale

interface OnLocaleChangedListener {
    fun onLocaleChanged(newLocale: String)
}

class MainActivity : AppCompatActivity(), OnLocaleChangedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var flagMenu: ImageView


    // View model
    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel

    private lateinit var userViewModelCR: com.rayo.rayoxml.cr.services.User.UserViewModel
    //private lateinit var authViewModelCR: com.rayo.rayoxml.cr.viewModels.AuthViewModel
    //val userViewModelFactoryCR = com.rayo.rayoxml.cr.services.User.UserViewModelFactory(com.rayo.rayoxml.cr.services.User.UserRepository(), PreferencesManager(this))

    private lateinit var userViewModelMX: com.rayo.rayoxml.mx.services.User.UserViewModel
    //private lateinit var authViewModelMX: com.rayo.rayoxml.mx.viewModels.AuthViewModel

    private var hasNavigated = false // Logout flag

    private var hasNavigatedCountry = false  // Flag to track navigation

    private var observerRanAtLeastOnce = false

    private val COUNTRY_RECEIVED = "countryReceived"
    private var countryReceived = ""

    override fun onLocaleChanged(newLocale: String) {
        Log.d("MainActivity", "Detectado cambio de país")
        countryReceived = newLocale

        observerRanAtLeastOnce = true
        setLocale(this, "es", newLocale)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the variable's value to outState
        outState.putString(COUNTRY_RECEIVED, countryReceived)
        outState.putBoolean("observerRanAtLeastOnce", observerRanAtLeastOnce)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        /*intent?.getStringExtra("locale")?.let { newLocale ->
            val locale = Locale(newLocale)
            //updateLocale(locale)
            Log.d("MainActivity", "Detectado cambio de país 2")
        }*/

        preferencesManager = PreferencesManager(this)

        val factory = AuthViewModelFactory(preferencesManager)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        //preferencesManager = PreferencesManager(this)
        lifecycleScope.launch {
            authViewModel.selectedCountry.collect { country ->
                if (authViewModel.isLoggedIn.firstOrNull() == false) {
                    Log.d("MainActivity", "🔄 Stopping country observer, user logged out.")

                    val preferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    val lastCountry = preferences.getString("LAST_COUNTRY", null)
                    Log.d("MainActivity", "Last country: ${lastCountry}")

                    if (::flagMenu.isInitialized) {
                        flagMenu.setImageResource(
                            when (country?.name) {
                                "Colombia" -> R.drawable.flag_colombia_svg
                                "Costa Rica" -> R.drawable.flag_costa_rica_svg
                                "México" -> R.drawable.flag_mexico_svg
                                else -> R.drawable.flag_colombia_svg
                            }
                        )
                    }

                    var newCountry = Country("", R.drawable.flag_colombia_svg)
                    when (lastCountry) {
                        "Colombia" -> {
                            newCountry = Country(lastCountry, R.drawable.flag_colombia_svg)
                            CreditParameterManager.setSelectedCountry("co")
                            CreditInformationManager.setSelectedCountry("co")
                        }
                        "Costa Rica" -> {
                            newCountry = Country(lastCountry, R.drawable.flag_costa_rica_svg)
                            CreditParameterManager.setSelectedCountry("cr")
                            CreditInformationManager.setSelectedCountry("cr")
                        }
                        "México" -> {
                            newCountry = Country(lastCountry, R.drawable.flag_mexico_svg)
                            CreditParameterManager.setSelectedCountry("mx")
                            CreditInformationManager.setSelectedCountry("mx")
                        }
                        else -> {
                            //throw IllegalArgumentException("País inválido")
                            newCountry = Country("Colombia", R.drawable.flag_colombia_svg)
                            CreditParameterManager.setSelectedCountry("co")
                            CreditInformationManager.setSelectedCountry("co")
                        }
                    }

                    if (Build.VERSION.SDK_INT in Build.VERSION_CODES.R..Build.VERSION_CODES.S_V2) {
                        // Solo Android 12 (S) y 12L (S_V2)
                        Log.d("UI", "Android 12, se ejecuta")
                        setLocaleAndRestartIfNeeded(newCountry.name, CreditParameterManager.getSelectedCountry())
                    } else {
                        // Todas las demás versiones
                        Log.d("UI", "Android diferente de 12, no se ejecuta")
                    }

                    Log.d("MainActivity", "savedInstanceState: ${savedInstanceState == null}, hasNavigatedCountry: ${hasNavigatedCountry}")
                    if (savedInstanceState == null &&
                        !hasNavigatedCountry &&
                        Build.VERSION.SDK_INT !in Build.VERSION_CODES.R..Build.VERSION_CODES.S_V2
                    ) {
                        Log.d("MainActivity", "Ejecutando setLocaleAndRestartIfNeeded()")
                        setLocaleAndRestartIfNeeded(newCountry.name, CreditParameterManager.getSelectedCountry())
                        hasNavigatedCountry = true  // Prevent multiple navigations
                    }
                    return@collect // Stop processing if logged out
                }

                if (country != null) {
                    Log.d("MainActivity", "COD País: ${CreditParameterManager.getSelectedCountry()}, Nombre: ${country.name}")

                    // Fix pantalla en blanco
                    if (Build.VERSION.SDK_INT in Build.VERSION_CODES.R..Build.VERSION_CODES.S_V2) {
                        // Solo Android 12 (S) y 12L (S_V2)
                        Log.d("UI", "Android 12, se ejecuta")
                        setLocaleAndRestartIfNeeded(country.name, CreditParameterManager.getSelectedCountry())
                    } else {
                        // Todas las demás versiones
                        Log.d("UI", "Android diferente de 12, no se ejecuta")
                    }

                    if (savedInstanceState == null && !hasNavigatedCountry) {
                        hasNavigatedCountry = true  // Prevent multiple navigations
                    }
                } else {
                    Log.d("MainActivity", "País no seleccionado, cargando co por defecto")
                    val newCountry = Country("Colombia", R.drawable.flag_colombia_svg)
                    authViewModel.saveSelectedCountry(newCountry)
                    if (savedInstanceState == null && !hasNavigatedCountry) {
                        hasNavigatedCountry = true  // Prevent multiple navigations
                    }
                }
            }
        }

        /*setGlobalTextSize(findViewById(android.R.id.content), 12f)*/

        // Permiso escritura para generación de paz y salvo
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
        }*/

        if (savedInstanceState != null) {
            // Restore the saved variable value
            countryReceived = savedInstanceState.getString(COUNTRY_RECEIVED, "")
            observerRanAtLeastOnce = savedInstanceState.getBoolean("observerRanAtLeastOnce", false)
        }
    }

    private fun observeLoginState() {

        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NavController", "Navigated to: ${destination.label}")
        }

        // Inicializar el ViewModel con un factory
        val repository = UserRepository()
        val factory = UserViewModelFactory(repository, preferencesManager)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        val repositoryCR = com.rayo.rayoxml.cr.services.User.UserRepository()
        val factoryCR = com.rayo.rayoxml.cr.services.User.UserViewModelFactory(repositoryCR, preferencesManager)
        userViewModelCR = ViewModelProvider(this, factoryCR)[com.rayo.rayoxml.cr.services.User.UserViewModel::class.java]

        val repositoryMX = com.rayo.rayoxml.mx.services.User.UserRepository()
        val factoryMX = com.rayo.rayoxml.mx.services.User.UserViewModelFactory(repositoryMX, preferencesManager)
        userViewModelMX = ViewModelProvider(this, factoryMX)[com.rayo.rayoxml.mx.services.User.UserViewModel::class.java]

        // Observe username updates
        userViewModelCR.userFullName.observe(this) { name ->
            binding.toolbarProfile.textUserName.text = name
            Log.d("MainActivity", "Asignando userFullName")
        }

        lifecycleScope.launch {
            delay(1000)
            //if (!observerRanAtLeastOnce) {
                //observerRanAtLeastOnce = true
                authViewModel.isLoggedIn.collectLatest { isLoggedIn ->
                //val isLoggedIn = authViewModel.isLoggedIn.firstOrNull()
                    if (isLoggedIn) {
                        hasNavigated = false // Reset when logged in
                        Log.d("MainActivity", "✅ User is logged in!")

                        val user = authViewModel.user.firstOrNull()
                        if (user != null) {
                            Log.d("MainActivity", "✅ User loaded: ${user.name}, Country: ${user.country}")

                            when (user.country) {
                                "Colombia" -> userViewModel.getData(user.id)
                                "Costa Rica" -> {
                                    userViewModelCR.getData(user.id)
                                    userViewModelCR.setUserFullName(user.name)
                                }
                                "México" -> userViewModelMX.getData(user.id)
                                else -> throw IllegalArgumentException("País inválido")
                            }

                            setSessionMenuUI(true) // Update UI

                            // Asiganr nuevamente layouts de país seleccionado
                            val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                            navController = navHost.navController

                            val graphInflater = navController.navInflater
                            val navGraph = when (user.country) {
                                "Colombia" -> graphInflater.inflate(R.navigation.main_graph_co)
                                "Costa Rica" -> graphInflater.inflate(R.navigation.main_graph_cr)
                                "México" -> graphInflater.inflate(R.navigation.main_graph_mx)
                                else -> graphInflater.inflate(R.navigation.main_graph_co) // throw IllegalArgumentException("País inválido")
                            }
                            navController.graph = navGraph

                            val headerView = binding.navigationView.getHeaderView(0)
                            flagMenu = headerView.findViewById(R.id.selectImageViewFlag)

                            navController.navigate(R.id.homeFragment)
                        } else {
                            Log.d("MainActivity", "⚠️ No user data found yet.")
                            //recreate()
                            //navController.navigate(R.id.loginFragment)
                        }
                    }else {
                        /*Log.d("MainActivity", "🔄 User logged out. Redirecting to Login...")

                        //val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                        //navController = navHost.navController
                        val currentDest = navController.currentDestination?.id
                        if (currentDest != R.id.loginFragment) {
                            Log.d("MainActivity", "Recreating")
                            recreate()
                            navController.navigate(R.id.loginFragment)
                        }*/
                        if (!hasNavigated) { // Ensure navigation happens only once
                            hasNavigated = true // Prevent repeated navigation

                            Log.d("MainActivity", "🔄 User logged out. Redirecting to Login...")

                            //
                            Log.d("MainActivity", "cód país obtenido: ${CreditParameterManager.getSelectedCountry()}")
                            val graphInflater = navController.navInflater
                            val navGraph = when (CreditParameterManager.getSelectedCountry()) {
                                "co" -> graphInflater.inflate(R.navigation.main_graph_co)
                                "cr" -> graphInflater.inflate(R.navigation.main_graph_cr)
                                "mx" -> graphInflater.inflate(R.navigation.main_graph_mx)
                                else -> throw IllegalArgumentException("País inválido")
                            }
                            navController.graph = navGraph
                            //

                            val headerView = binding.navigationView.getHeaderView(0)
                            flagMenu = headerView.findViewById(R.id.selectImageViewFlag)

                            val sharedPreferences = this@MainActivity.getSharedPreferences("Onboarding", Context.MODE_PRIVATE)
                            val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)
                            Log.d("MainActivity", "isFirstTime: $isFirstTime")

                            val currentDest = navController.currentDestination?.id

                            Log.d("MainActivity", "Aplicando cambio de país: $countryReceived")

                            Handler(Looper.getMainLooper()).postDelayed({

                                if (currentDest != R.id.loginFragment) {
                                    Log.d("MainActivity", "Recreating")
                                    //recreate()
                                    setSessionMenuUI(false)
                                    //navController.navigate(R.id.loginFragment)

                                    ////navController.navigate(R.id.action_loadingFragment_to_viewPagerFragment)
                                    if (isFirstTime) {
                                        navController.navigate(R.id.action_loadingFragment_to_viewPagerFragment)
                                    } else {
                                        val previousDestination = navController.previousBackStackEntry?.destination
                                        Log.d("MainActivity", "Pantalla previa: $previousDestination")
                                        val currentDestination = navController.currentBackStackEntry?.destination
                                        Log.d("MainActivity", "Pantalla sigte: $currentDestination")

                                        //navController.navigate(R.id.action_loadingFragment_to_loanFragment)
                                        if( CreditParameterManager.getSelectedCountry() == "co" ){
                                            if( (previousDestination == null || previousDestination.id != R.id.loadingFragment)
                                                && (currentDestination == null || currentDestination.id != R.id.loanFragment) ){
                                                navController.navigate(R.id.viewPagerFragment)
                                            }else{
                                                Log.d("MainActivity", "Pantalla previa es Loading o sigte loan")
                                            }
                                        }
                                        else if( CreditParameterManager.getSelectedCountry() == "cr" ){

                                            val preferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                                            val lastCountry = preferences.getString("LAST_COUNTRY", null)
                                            if( lastCountry == "Costa Rica" && countryReceived != "CR" ){
                                                Log.d("MainActivity", "Se redirecciona ya que no se cambió de país hacia CR")
                                                navController.navigate(R.id.viewPagerFragment)
                                            }else{
                                                Log.d("MainActivity", "No se redirecciona ya que se cambió de país hacia CR")
                                                //navController.navigate(R.id.loanFragment)
                                                navController.navigate(R.id.action_loadingFragment_to_loanFragment)
                                            }
                                        }
                                    }
                                }else{
                                    Log.d("MainActivity", "Reloading")
                                    setSessionMenuUI(false)
                                    //navController.navigate(R.id.loginFragment)
                                    try {
                                        ////navController.navigate(R.id.action_loadingFragment_to_viewPagerFragment)
                                        if (isFirstTime) {
                                            navController.navigate(R.id.action_loadingFragment_to_viewPagerFragment)
                                        } else {
                                            //navController.navigate(R.id.action_loadingFragment_to_loanFragment)
                                            navController.navigate(R.id.action_loadingFragment_to_viewPagerFragment)
                                        }
                                    }catch (e: Exception){
                                        Log.e("MainActivity", "Error al redireccionar a viewPagerFragment", e)
                                        navController.navigate(R.id.loginFragment)
                                    }
                                }
                            }, 1000)
                        }
                    }
                    //return@collectLatest // Stop processing if logged out
                }
            //}
            /*else{
                Log.d("MainActivity", "Deteniendo observer userData")
            }*/
        }
    }

    private fun isLocaleSet(language: String, country: String): Boolean {
        val currentLocale = resources.configuration.locales[0]
        return currentLocale.language == language && currentLocale.country == country
    }

    private fun setLocaleAndRestartIfNeeded(countryName: String, countryCode: String) {
        val savedLanguage = "es"

        // Retrieve last used country from SharedPreferences (or DataStore)
        val preferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastCountry = preferences.getString("LAST_COUNTRY", null)

        // Only restart if the country has actually changed
        if (lastCountry == null || lastCountry != countryName) {
            setLocale(this, savedLanguage, countryCode)

            Log.d("MainActivity", "Cambiando país a: $countryName")

            // Save new country in preferences to prevent looping
            preferences.edit().putString("LAST_COUNTRY", countryName).apply()

            /*Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }, 200)*/

        } else {
            Log.d("MainActivity", "País actual: $countryName")
            Log.d("MainActivity", "savedLanguage: $savedLanguage")
            Log.d("MainActivity", "countryCode: $countryCode")
            setLocale(this, savedLanguage, countryCode) // Move this here to ensure locale is updated even if no restart is needed
        }

        Log.d("LocaleCheck", "Current locale: ${resources.configuration.locales[0]}")

        // Inflate layout only after locale is set (only once)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        if (!observerRanAtLeastOnce) {
            observerRanAtLeastOnce = true
            observeLoginState() // ✅ Call function to observe login state
        }

        // Configuración del botón de retroceso en la toolbar
        findViewById<ImageView>(R.id.back_toolbar_icon).setOnClickListener {
            Log.d("ToolbarClick", "Botón de retroceso presionado")
            onBackPressedDispatcher.onBackPressed()
        }

        try {
            when (countryName) {
                "Colombia" -> loadViewData();
                "Costa Rica" -> loadViewDataCR();
                "México" -> loadViewDataMX();
                else -> throw IllegalArgumentException("País desconocido")
            }
            Log.d("MainActivity", "Cargando datos de usuario ${countryName}")

        }catch (e: Exception){
            println("❗ Exception: ${e.message}")
        }
    }

    fun loadViewData() {
        // Observe user data
        userViewModel.userData.observe(this) { user ->
            if (user != null) {
                Log.d("MainActivity", "Cargando datos de usuario CO")
                binding.toolbarProfile.textUserName.text = "${user.nombre} ${user.apellidos}"
            }
        }
    }

    fun loadViewDataCR() {
        // Observe user data
        userViewModelCR.userData.observe(this) { user ->
            if (user != null) {
                Log.d("MainActivity", "Cargando datos de usuario CR: ${user}")
                binding.toolbarProfile.textUserName.text = "${user.nombre}"
            }
        }
    }

    fun loadViewDataMX() {
        // Observe user data
        userViewModelMX.userData.observe(this) { user ->
            if (user != null) {
                Log.d("MainActivity", "Cargando datos de usuario MX")
                binding.toolbarProfile.textUserName.text = "${user.nombre} ${user.apellidos}"
            }
        }
    }

    fun getAndroidVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    private fun initRemoteConfig() {
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Credit params
                val jsonString = remoteConfig.getString("credit_parameters")
                CreditParameterManager.saveRemoteConfigData(jsonString)
                Log.d("MainActivity", "Remote Config Loaded: $jsonString")

                // Credit information
                val jsonStringCreditInfo = remoteConfig.getString("credit_information")
                CreditInformationManager.saveRemoteConfigData(jsonStringCreditInfo)
                Log.d("MainActivity", "Remote Config Loaded: $jsonStringCreditInfo")
            }
        }
    }

    private fun observeNavigation() {
        viewModel.navigationEvent.observe(this) { destinationId ->
            destinationId?.let {
                navController.navigate(it)
                viewModel.clearNavigationEvent()
            }
        }

        viewModel.toolbarVisibility.observe(this) { visibilityMap ->
            binding.apply {
                findViewById<View>(R.id.toolbar_menu).visibility = if (visibilityMap["toolbarMenu"] == true) View.VISIBLE else View.GONE
                findViewById<View>(R.id.toolbar_profile).visibility = if (visibilityMap["toolbarProfile"] == true) View.VISIBLE else View.GONE
                findViewById<View>(R.id.toolbar_personal_profile).visibility = if (visibilityMap["toolbarPersonalProfile"] == true) View.VISIBLE else View.GONE
                findViewById<View>(R.id.toolbar_layout).visibility = if (visibilityMap["toolbarLayout"] == true) View.VISIBLE else View.GONE

                bottomNavViewLogin.visibility = if (visibilityMap["bottomNav"] == true) View.VISIBLE else View.GONE
            }
        }

        viewModel.drawerState.observe(this) { isOpen ->
            if (isOpen) drawerLayout.openDrawer(GravityCompat.END) else drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun initNavigation() {
        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHost.navController
        binding.bottomNavViewLogin.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("Navigation", "Current destination: ${resources.getResourceName(destination.id)}")
            viewModel.updateToolbarVisibility(destination.id)
            viewModel.updateMenuItemVisibility(destination.id)
        }

        // Asignar función para abir whathsapp sin sobreescribir el resto de menús
        binding.bottomNavViewLogin.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.contactButton -> {
                    openWhatsApp() // Abrir WhatsApp
                    true
                }
                else -> {
                    // ?Navegación por defecto para el resto de items
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    true
                }
            }
        }

        viewModel.isMenuItemVisible.observe(this) { isVisible ->
            val navigationView = findViewById<NavigationView>(R.id.navigation_view)
            val menuItem = navigationView.menu.findItem(R.id.nav_logout)
            menuItem?.isVisible = isVisible
        }
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        val menuIcon = findViewById<ImageView>(R.id.menu_icon)
        val menuIconHome = findViewById<ImageView>(R.id.menu_icon_home)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val navigationLogout = findViewById<MaterialButton>(R.id.nav_logout)

        // Configurar el comportamiento del botón de menú manualmente
        val toggleClickListener = View.OnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Asegurar que solo se asigna una vez el listener
        menuIcon.setOnClickListener(toggleClickListener)
        menuIconHome.setOnClickListener(toggleClickListener)

        navigationView.itemIconTintList = null
        navigationLogout.iconTint = null
        navigationView.setupWithNavController(navController)

        val headerView: View = navigationView.getHeaderView(0)
        val imageViewFlag: ImageView = headerView.findViewById(R.id.selectImageViewFlag)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            /*if (menuItem.itemId == R.id.nav_login) navController.navigate(R.id.loginFragment)*/
            when (menuItem.itemId) {
                R.id.nav_login -> navController.navigate(R.id.loginFragment)
                R.id.nav_loan_types -> navController.navigate(R.id.loanTypesFragment)
                R.id.nav_benefit -> navController.navigate(R.id.benefitFragment)
                R.id.nav_contact -> {
                    openWhatsApp()
                    return@setNavigationItemSelectedListener true
                }
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }


        navigationLogout.setOnClickListener {
            Log.d("MainActivity", "Cerrando sesión")
            lifecycleScope.launch {

                withContext(Dispatchers.Main) {
                    //preferencesManager.logout()
                    viewModel.onLogoutClicked()
                    userViewModel.clearData()
                    setSessionMenuUI(false)

                    authViewModel.logout()

                    hasNavigated = false

                    authViewModel.isLoggedIn.first { isLoggedIn ->
                        Log.d("UI", "🔄 UI Updated: isLoggedIn = $isLoggedIn")
                        !isLoggedIn // Continue only when isLoggedIn becomes false
                    }

                    //recreate()
                    //navController.navigate(R.id.loginFragment) // Redirect to login

                    /*val currentDest = navController.currentDestination?.id
                    if (currentDest != R.id.loginFragment) {
                        Log.d("MainActivity", "Recreating")
                        //recreate()
                        navController.navigate(R.id.loginFragment)
                        recreate()
                    }*/

                    //navController.navigate(R.id.loginFragment) // Redirect to login
                }
            }
        }

        val closeDrawerIcon: ImageView = headerView.findViewById(R.id.close_drawer_icon)
        closeDrawerIcon.setOnClickListener { drawerLayout.closeDrawer(GravityCompat.END) }
    }

    private fun setSessionMenuUI(isLoggedIn: Boolean){

        // Controlar botones de login/logout
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val menu = navigationView.menu
        val logoutButton = findViewById<Button>(R.id.nav_logout)

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

    private fun openWhatsApp() {
        val phoneNumber = getString(R.string.contact_number_colombia) // Obtener país
        val message = getString(R.string.contact_message)
        val encodedMessage = Uri.encode(message)

        val url = "https://wa.me/$phoneNumber?text=$encodedMessage"

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                setPackage("com.whatsapp") // Abrir WhatsApp
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // If WhatsApp is not installed, open in browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
    }

    // Asignar país
    fun setLocale(context: Context, language: String, countryCode: String) {
        val locale = Locale(language, countryCode.uppercase())
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Recrear actividad
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.S..Build.VERSION_CODES.S_V2) {
            // Solo Android 12 (S) y 12L (S_V2)
            Log.d("UI", "Android 12, se ejecuta")
            recreate()
        } else {
            // Todas las demás versiones
            Log.d("UI", "Android diferente de 12, no se ejecuta")
        }

        /*if (Build.VERSION.SDK_INT in (Build.VERSION_CODES.Q + 1)..Build.VERSION_CODES.S_V2) {
            // Solo Android 11 (R) y 12 (S, S_V2)
            Log.d("UI", "Android 11-12, se ejecuta")
            recreate()
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // BAKLAVA
            // Android 10 o menor, o entre 13 y UDC
            Log.d("UI", "Android menor a 11 o mayor a 12, no se ejecuta")
        }*/
    }
}
