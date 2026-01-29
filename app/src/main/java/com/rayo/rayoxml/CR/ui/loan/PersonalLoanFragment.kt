package com.rayo.rayoxml.cr.ui.loan

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.services.Auth.Prestamo
import com.rayo.rayoxml.databinding.FragmentPersonalLoanBinding
//import com.rayo.rayoxml.cr.services.User.Prestamo
import com.rayo.rayoxml.cr.services.User.UserRepository
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.services.User.UserViewModelFactory
import com.rayo.rayoxml.cr.ui.dialogs.FilterBottomSheetDialog
import com.rayo.rayoxml.cr.ui.dialogs.OrderBottomSheetDialog
import com.rayo.rayoxml.cr.ui.slider_cards.CardLoanAdapter
import com.rayo.rayoxml.cr.viewModels.RenewalViewModel
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.utils.navigateToRenewalFragment

class PersonalLoanFragment : Fragment() {

    private var _binding: FragmentPersonalLoanBinding? = null
    private val binding get() = _binding!!

    // viewmodel de datos de usuario
    private lateinit var userViewModel: UserViewModel
    // Modelo compartido renovaciones
    private val renewalViewModel: RenewalViewModel by activityViewModels()

    // Mensaje de filtro sin préstamos
    private val NO_LOANS_TITLE = "No se encontraron resultados"
    private val NO_LOANS_MESSAGE = "Parece que no hay coincidencias. Prueba cambiando los filtros para encontrar lo que buscas."

    // Tipos de préstamo
    private val PRESTAMO_MINI = "Préstamo mini"
    private val PRESTAMO_RAYO_PLUS = "Préstamo Rayo Plus"
    private val PRESTAMO_LARGO_PLAZO = "Préstamo Largo Plazo (PLP)"

    private val PENDING_LOAN_STATUS = "Pendiente"
    private val COMPLETED_LOAN_STATUS = "Cancelado"
    private val REVISION_LOAN_STATUS = "En Revisión"
    private val COMPLETED_PAYMENT_STATUS = "Pagado"

    // Lista completa de préstamos
    var listaPrestamosCompleta: MutableList<Prestamo> = mutableListOf()
    // Lista filtrada
    var listaPrestamos: MutableList<Prestamo> = mutableListOf()

    var adapter = CardLoanAdapter(
        mutableListOf(),
        onItemClickListener = {}
    )
    /*var adapter = CardLoanAdapter(mutableListOf()) { prestamo ->
        // Navegar al nuevo Fragment con el préstamo seleccionado
        if (prestamo != null) {
            val bundle = Bundle().apply {
                //Log.d("PersonalLoanFragment", "Préstamo seleccionado: ${prestamo}")
                putParcelable("prestamoSeleccionado", prestamo) // Asegúrate de que Prestamo implemente Parcelable
            }
            findNavController().navigate(R.id.action_personalLoanFragment_to_loanDetailFragment, bundle)
        } else {
            Log.e("NavigationError", "El objeto prestamo es NULL ${prestamo}")
        }
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalLoanBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val recyclerView: RecyclerView = view.findViewById(R.id.itemCardLoanRecyclerView)
        val recyclerView = binding.itemCardLoanRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Ocultar la barra de desplazamiento, pero permitir gestos para usarla
        recyclerView.apply {
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        // Campos para usuario sin préstamos
        val noLoansText = binding.loginTitle
        val noLoansText2 = binding.loginText
        val noLoansButton = binding.firstButtonLoan

        // Obtener ViewModel compartido con la actividad
        val repository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(repository, preferencesManager)
        userViewModel = ViewModelProvider(requireActivity(), factory)[UserViewModel::class.java]

        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            Log.d("PersonalLoanFragment", "Préstamos mini: ${user?.prestamos?.size}")
            Log.d("PersonalLoanFragment", "Préstamos Rayo Plus: ${user?.prestamosRP?.size}")
            Log.d("PersonalLoanFragment", "Préstamos Largo Plazo: ${user?.prestamosPLP?.size}")

            if (user?.prestamos != null) {
                // Asignar tipo
                user.prestamos.forEach { it.tipo = PRESTAMO_MINI }
            }
            if (user?.prestamosRP != null) {
                // Asignar tipo
                user.prestamosRP.forEach { it.tipo = PRESTAMO_RAYO_PLUS }
            }
            if (user?.prestamosPLP != null) {
                // Asignar tipo
                user.prestamosPLP.forEach { it.tipo = PRESTAMO_LARGO_PLAZO }
            }

            if (user != null) {

                if ( user.prestamos.isNullOrEmpty() && user.prestamosRP.isNullOrEmpty()
                    && user.prestamosPLP.isNullOrEmpty() ) {
                    // All lists are null or empty
                } else {
                    listaPrestamos.clear()
                    listaPrestamos.addAll(user.prestamos!!.filterNotNull() ?: emptyList())
                    listaPrestamos.addAll(user.prestamosRP!!.filterNotNull() ?: emptyList())
                    listaPrestamos.addAll(user.prestamosPLP!!.filterNotNull() ?: emptyList())

                    // Asignar datos a lista completa
                    listaPrestamosCompleta = listaPrestamos

                    // Asignar a listado de adapter (usando variable local)
                    //val cardList = listaPrestamos
                    //val adapter = CardLoanAdapter(cardList)

                    //Log.d("PersonalLoanFragment", "RecyclerView encontrado: $recyclerView")
                    if(listaPrestamos.isNotEmpty()){
                        recyclerView.post {
                            noLoansText.visibility = View.GONE
                            noLoansText2.visibility = View.GONE
                            noLoansButton.visibility = View.GONE

                            recyclerView.visibility = View.VISIBLE

                            // Actualizar
                            adapter = CardLoanAdapter(mutableListOf()) { prestamo ->
                                // Navegar al nuevo Fragment con el préstamo seleccionado
                                val bundle = Bundle().apply {
                                    Log.d("PersonalLoanFragment", "Préstamo seleccionado: ${prestamo}")
                                    putParcelable("prestamoSeleccionado", prestamo) // Asegúrate de que Prestamo implemente Parcelable
                                }
                                // Inhabilitar navegación
                                findNavController().navigate(
                                    R.id.action_personalLoanFragment_to_loanDetailFragment,
                                    bundle,
                                    NavOptions.Builder()
                                        //.setPopUpTo(R.id.personalLoanFragment, true) // Limpia historial de navegación
                                        .build()
                                )
                            }

                            recyclerView.adapter = null
                            recyclerView.adapter = adapter
                            //adapter.notifyDataSetChanged()
                            adapter.updateData(listaPrestamos)
                            adapter.notifyDataSetChanged()
                        }
                    }

                    //Log.d("PersonalLoanFragment", cardList.toString())
                }
            }
        }

        binding.orderButtonLoan.setOnClickListener {
            showOrderBottomSheetDialog()
            //Log.d("PersonalLoanFragment", "Click filtro")
        }

        binding.filterButtonLoan.setOnClickListener {
            showFilterBottomSheetDialog()
        }

        // Solicitar crédito
        binding.firstButtonLoan.setOnClickListener {
            renewalViewModel.setLoanType("MINI")
            navigateToRenewalFragment()
        }

        // Listen for sorting selection
        parentFragmentManager.setFragmentResultListener("orderRequestKey", viewLifecycleOwner) { _, bundle ->
            val selectedOrder = bundle.getString("selectedOrder") ?: return@setFragmentResultListener
            applySorting(selectedOrder)
        }

        // Listen for filter selection
        parentFragmentManager.setFragmentResultListener("filterRequestKey", viewLifecycleOwner) { _, bundle ->
            val selectedTypes: MutableList<String> = bundle.getStringArrayList("typeFilters")?.toMutableList() ?: mutableListOf()
            val selectedStatus: MutableList<String> = bundle.getStringArrayList("statusFilters")?.toMutableList() ?: mutableListOf()

            Log.d("FragmentResult", "selectedTypes received: $selectedTypes")
            Log.d("FragmentResult", "selectedStatus received: $selectedStatus")
            applyFilters(selectedTypes, selectedStatus)
        }
    }

    private fun applySorting(order: String) {
        //var newList: MutableList<Prestamo> = mutableListOf()
        when (order) {
            "Monto más alto" -> listaPrestamos = listaPrestamos.sortedByDescending { it.montoPrestado.toDoubleOrNull() ?: 0.0 }.toMutableList()
            "Monto más bajo" -> listaPrestamos = listaPrestamos.sortedBy { it.montoPrestado.toDoubleOrNull() ?: 0.0 }.toMutableList()
            "Con más cuotas primero" -> listaPrestamos = listaPrestamos.sortedByDescending { it.pagos.size }.toMutableList()
            "Con menos cuotas primero" -> listaPrestamos = listaPrestamos.sortedBy { it.pagos.size }.toMutableList()
            "Recientes primero" -> listaPrestamos = listaPrestamos.sortedByDescending { it.fechaDeposito }.toMutableList()
            "Antiguos primero" -> listaPrestamos = listaPrestamos.sortedBy { it.fechaDeposito }.toMutableList()
        }

        adapter.updateData(listaPrestamos) // Refresh RecyclerView
        binding.itemCardLoanRecyclerView.scrollToPosition(0) // Reset scroll to top
    }

    fun applyFilters(selectedTypes: List<String>, selectedStatus: List<String>) {
        // Aplicar filtros
        /*if (selectedTypes.isEmpty() && selectedStatus.isEmpty()) {
            // Cargar lista original
            listaPrestamos = listaPrestamosCompleta
        } else {
            listaPrestamos = listaPrestamos.filter { prestamo ->
                (selectedTypes.isEmpty() || prestamo.tipo in selectedTypes) &&
                        (selectedStatus.isEmpty() || prestamo.estado in selectedStatus)
            }.toMutableList()
        }*/

        // Estado "En revisión"
        val normalizedSelectedStatus = selectedStatus.map {
            if (it == "En revisión") "En Revisión" else it
        }

        // Aplicar filtros a lista completa
        listaPrestamos = listaPrestamosCompleta

        listaPrestamos = listaPrestamos.filter { prestamo ->
            (selectedTypes.isEmpty() || prestamo.tipo in selectedTypes) &&
                    (normalizedSelectedStatus.isEmpty() || getLoanStatus(prestamo) in normalizedSelectedStatus) // in selectedStatus
        }.toMutableList()

        adapter.updateData(listaPrestamos) // Refresh RecyclerView
        binding.itemCardLoanRecyclerView.scrollToPosition(0) // Reset scroll to top

        // Habilitar campo de usuario sin préstamos
        val noLoansText = binding.loginTitle
        val noLoansText2 = binding.loginText
        //val noLoansButton = binding.firstButtonLoan

        if(listaPrestamos.isEmpty()){
            // Cambiar mensaje
            noLoansText.text = NO_LOANS_TITLE
            noLoansText2.text = NO_LOANS_MESSAGE
            // Camnbiar visibilidad
            noLoansText.visibility = View.VISIBLE
            noLoansText2.visibility = View.VISIBLE
            //noLoansButton.visibility = View.VISIBLE
        }else{
            noLoansText.visibility = View.GONE
            noLoansText2.visibility = View.GONE
            //noLoansButton.visibility = View.GONE
        }
    }

    private fun showOrderBottomSheetDialog() {
        val dialog = OrderBottomSheetDialog()
        dialog.show(parentFragmentManager, "OrderBottomSheetDialog")
    }

    private fun showFilterBottomSheetDialog() {
        val dialog = FilterBottomSheetDialog()
        dialog.show(parentFragmentManager, "FilterAccordionBottomSheetDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Vaciar filtros
        val sharedPref = requireContext().getSharedPreferences("FilterPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("selectedLoanTypes")
            remove("selectedLoanStatus")
            apply()
        }
    }

    private fun getLoanStatus(loan: Prestamo): String {
        return if (loan.pagos.any { it.estado != COMPLETED_PAYMENT_STATUS }) {
            PENDING_LOAN_STATUS
        } else {
            if(loan.fechaDeposito == "" && loan.pagos.isEmpty()) REVISION_LOAN_STATUS
            else COMPLETED_LOAN_STATUS
        }
    }
}