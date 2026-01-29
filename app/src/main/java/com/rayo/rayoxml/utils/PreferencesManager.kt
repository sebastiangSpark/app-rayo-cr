package com.rayo.rayoxml.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rayo.rayoxml.co.models.Country
import com.rayo.rayoxml.co.models.LoanCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "userData")

// Gestionar datos de sesión
data class User(
    val id: String,
    val name: String,
    val email: String,
    val country: String = ""
)

class PreferencesManager(private val context: Context) {

    companion object {
        /*SELECT COUNTRY*/
        private val SELECTED_COUNTRY_NAME = stringPreferencesKey("selectedCountryName")
        private val SELECTED_COUNTRY_FLAG_RES_ID = intPreferencesKey("selectedCountryFlagResId")
        private val SELECTED_IMAGE_KEY = intPreferencesKey("selected_image")
        /*CARD LOAN*/
        private val HAS_ACTIVE_LOAN = booleanPreferencesKey("has_active_loan")
        private val LOAN_TITLE = stringPreferencesKey("loan_title")
        private val LOAN_STATE = stringPreferencesKey("loan_state")
        private val LOAN_AMOUNT = stringPreferencesKey("loan_amount")
        private val LOAN_PAYMENT = stringPreferencesKey("loan_payment")
        private val LOAN_DATE = stringPreferencesKey("loan_date")
        // Datos de sesión
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    // Save user data
    suspend fun saveUserData(userId: String, userName: String, userEmail: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USER_NAME] = userName
            preferences[USER_EMAIL] = userEmail
            preferences[IS_LOGGED_IN] = true
        }
    }

    // Retrieve login state
    fun isLoggedIn(): Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[IS_LOGGED_IN] ?: false }

    // Retrieve user data
    fun getUserData(): Flow<User?> = context.dataStore.data.map { preferences ->
        val userId = preferences[USER_ID]
        val userName = preferences[USER_NAME]
        val userEmail = preferences[USER_EMAIL]
        val countryName = preferences[SELECTED_COUNTRY_NAME]

        if (userId != null) {
            User(userId, userName ?: "", userEmail ?: "", countryName ?: "")
        } else {
            null
        }
    }

    // Logout user
    suspend fun logout() {
        val currentValue = context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }.first()
        Log.d("PreferencesManager", "🔍 Before logout, isLoggedIn = $currentValue")

        val currentCountry = context.dataStore.data.map { it[SELECTED_COUNTRY_NAME] }.firstOrNull()

        context.dataStore.edit { preferences ->
            preferences.clear() // Clears all preferences

            // Restore selected country if it was set
            currentCountry?.let {
                preferences[SELECTED_COUNTRY_NAME] = it
            }

            preferences[IS_LOGGED_IN] = false // Explicitly set to false
        }

        Log.d("PreferencesManager", "✅ Logout successful! Country retained: $currentCountry")

        val updatedValue = context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }.first()
        Log.d("PreferencesManager", "✅ After logout, isLoggedIn = $updatedValue")
    }

    // Obtener país seleccionado
    fun getSelectedCountry(): Flow<Country?> {
        return context.dataStore.data.map { preferences ->
            val name = preferences[SELECTED_COUNTRY_NAME]
            val flagResId = preferences[SELECTED_COUNTRY_FLAG_RES_ID] ?: -1

            if (name != null && flagResId != -1) {
                Country(name, flagResId)
            } else {
                null
            }
        }
    }

    // Guardar país seleccionado
    suspend fun saveSelectedCountry(country: Country) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_COUNTRY_NAME] = country.name
            preferences[SELECTED_COUNTRY_FLAG_RES_ID] = country.flagResId
        }
    }

    // Obtener imagen seleccionada
    val selectedImage: Flow<Int?> = context.dataStore.data
        .map { preferences -> preferences[SELECTED_IMAGE_KEY] }

    // Guardar imagen seleccionada
    suspend fun saveSelectedImage(imageResId: Int) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_IMAGE_KEY] = imageResId
        }
    }

    // Guardar información del préstamo
    suspend fun saveLoanCard(loanCard: LoanCard) {
        context.dataStore.edit { preferences ->
            preferences[HAS_ACTIVE_LOAN] = loanCard.hasActiveLoan
            preferences[LOAN_TITLE] = loanCard.title
            preferences[LOAN_STATE] = loanCard.state
            preferences[LOAN_AMOUNT] = loanCard.amount ?: ""
            preferences[LOAN_PAYMENT] = loanCard.payment ?: ""
            preferences[LOAN_DATE] = loanCard.date
        }
    }

    // Obtener información del préstamo
    fun getLoanCard(): Flow<LoanCard?> {
        return context.dataStore.data.map { preferences ->
            val hasActiveLoan = preferences[HAS_ACTIVE_LOAN] ?: false
            val title = preferences[LOAN_TITLE] ?: ""
            val state = preferences[LOAN_STATE] ?: ""
            val amount = preferences[LOAN_AMOUNT]
            val payment = preferences[LOAN_PAYMENT]
            val date = preferences[LOAN_DATE] ?: ""

            if (title.isNotEmpty() && date.isNotEmpty()) {
                LoanCard(hasActiveLoan, title, state, amount, payment, date)
            } else {
                null
            }
        }
    }
}