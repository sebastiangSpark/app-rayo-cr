package com.rayo.rayoxml.co.ui.preguntas

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rayo.rayoxml.R
import com.rayo.rayoxml.co.adapters.PreguntaAdapter
import com.rayo.rayoxml.co.services.Loan.Pregunta
import com.rayo.rayoxml.co.services.Loan.LoanRepository
import com.rayo.rayoxml.co.services.Loan.LoanStepOneViewModel
import com.rayo.rayoxml.co.services.Loan.LoanStepOneViewModelFactory
import com.rayo.rayoxml.co.services.Loan.QuestionsRequest
import com.rayo.rayoxml.co.services.Loan.QuestionsValidationRequest
import com.rayo.rayoxml.co.services.Loan.Respuesta
import com.rayo.rayoxml.co.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.co.ui.loan.outcome.LoanOutcome
import com.rayo.rayoxml.databinding.FragmentPreguntasBinding
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreguntasFragment : Fragment() {
    private var _binding: FragmentPreguntasBinding? = null
    private val binding get() = _binding!!
    private lateinit var preguntaAdapter: PreguntaAdapter
    //private val respuestasSeleccionadas = mutableMapOf<Int, String>()
    private val respuestasSeleccionadas = mutableMapOf<String, String>()

    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment

    private var formId: String? = ""
    private lateinit var loanViewModel: LoanStepOneViewModel
    private lateinit var repository: LoanRepository

    private var attempts: Int = 0
    private var maxAttempts: Int = 2

    // Lista de respuestas a enviar
    val respuestas = mutableListOf<Respuesta>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreguntasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        repository = LoanRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = LoanStepOneViewModelFactory(repository, preferencesManager)
        loanViewModel = ViewModelProvider(requireActivity(), factory)[LoanStepOneViewModel::class.java]

        loanViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                formId = user.formulario
                Log.d("PreguntasFragment", "ID form ${formId}")
                // Cargar lista de preguntas
                cargarPreguntas()
            }
        }

        loanViewModel.questionsVerificationAttempts.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                attempts = data
                Log.d("PreguntasFragment", "Intentos ${attempts}")
            }
        }
    }

    private fun validarYEnviarRespuestas(preguntas: List<Pregunta>) {
        when {
            respuestasSeleccionadas.size < preguntas.size -> {
                mostrarMensaje("Por favor responde todas las preguntas")
                scrollToFirstUnansweredQuestion(preguntas)
            }
            else -> enviarRespuestas()
        }
    }

    private fun scrollToFirstUnansweredQuestion(preguntas: List<Pregunta>) {
        //val firstUnanswered = preguntas.firstOrNull { it.idPregunta.toInt() !in respuestasSeleccionadas.keys }
        val firstUnanswered = preguntas.firstOrNull { it.idPregunta !in respuestasSeleccionadas.keys }
        firstUnanswered?.let { pregunta ->
            val position = preguntas.indexOfFirst { it.idPregunta == pregunta.idPregunta }
            binding.recyclerPreguntas.smoothScrollToPosition(position)
        }
    }

    private fun enviarRespuestas() {

        // Validar intentos
        if(attempts >= maxAttempts){
            gotoRejectedScreen()
        }

        Log.d("PreguntasFragment", "Respuestas: ${respuestasSeleccionadas.toString()}")
        // Lista de respuestas
        respuestas.clear()
        respuestasSeleccionadas.forEach { (idPregunta, idRespuesta) ->
            respuestas.add(Respuesta(idPregunta, idRespuesta))
        }
        Log.d("PreguntasFragment", "Respuestas a enviar: ${respuestas}")

        loadingDialog.show(parentFragmentManager, "LoadingDialog") // Mostrar loading

        try {
            val responsesRequest = QuestionsValidationRequest(
                formulario = formId!!,
                respuestas = respuestas
            )
            lifecycleScope.launch {
                val questionsValidationResponse = withContext(Dispatchers.IO) {
                    repository.getDataQuestionsValidation(responsesRequest)
                }

                Log.d("PreguntasFragment", "Questions validation response: ${questionsValidationResponse}")

                if(questionsValidationResponse != null){
                    // Registrar nuevo intento
                    loanViewModel.incrementQuestionsVerificationAttempts()
                }

                if(questionsValidationResponse != null && questionsValidationResponse.solicitud.codigo == "200"
                    && questionsValidationResponse.solicitud.result == "Solucion recibida"){
                    // Validar formulario
                    val responsesVerificationRequest = QuestionsRequest(
                        formulario = formId!!
                    )
                    val questionsVerificationResponse = withContext(Dispatchers.IO) {
                        repository.getDataQuestionsVerification(responsesVerificationRequest)
                    }
                    loadingDialog.dismiss()

                    Log.d("PreguntasFragment", "Questions verification (step 2) response: ${questionsVerificationResponse}")

                    if(questionsVerificationResponse != null && questionsVerificationResponse.solicitud.codigo == "200"
                        && questionsVerificationResponse.solicitud.resultado == "Valido"){
                        // continuar
                        findNavController().navigate(
                            R.id.action_PreguntasFragment_to_formFragment,
                            bundleOf("goToStep3" to true)
                        )
                    }else{
                        // validar intentos
                        validateAttempts()
                    }
                }else{
                    loadingDialog.dismiss()
                    // validar intentos
                    validateAttempts()
                }
            }

            //mostrarMensaje("Encuesta enviada correctamente")
        }catch (e: Exception) {
            loadingDialog.dismiss() // Ocultar en caso de error
            Log.e("PreguntasFragment", "Error obteniendo datos", e)

            gotoRejectedScreen()
        }
    }

    private fun gotoRejectedScreen(){
        val rejectBundle = Bundle().apply {
            putSerializable("outcome", LoanOutcome.REJECTED)
        }
        findNavController().navigate(R.id.loanOutcomeFragment, rejectBundle)
    }

    private fun validateAttempts(){
        if(attempts >= maxAttempts){
            // Redireccionar a rechazo
            gotoRejectedScreen()
        }else{

            // Modal de reintentar
            showValidationFailedDialog()

            /*// Redireccionar a reintentar validación
            findNavController().navigate(
                R.id.action_PreguntasFragment_to_formFragment,
                bundleOf("goToStep2" to true)
            )*/
        }
    }

    private fun showValidationFailedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Validación fallida")
            .setMessage("Ocurrió un error al validar. ¿Deseas reintentar?")
            .setCancelable(false)
            .setPositiveButton("Reintentar") { _, _ ->
                // Redireccionar a reintentar validación
                findNavController().navigate(
                    R.id.action_PreguntasFragment_to_formFragment,
                    bundleOf("goToStep2" to true)
                )
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                gotoRejectedScreen()
            }
            .show()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun cargarPreguntas(){
        // cargar preguntas
        val questionsRequest = QuestionsRequest(
            formulario = formId!!
        )
        lifecycleScope.launch {
            val questionsResponse = withContext(Dispatchers.IO) {
                repository.getDataQuestions(questionsRequest)
            }

            /*val questionsResponse = QuestionResponse(
                codigo = "200",
                preguntas = listOf(
                    Pregunta(
                        idPregunta = "a0RO4000004jO2KMAU",
                        pregunta = "HACE CUANTO TIEMPO TIENE USTED UNA CUENTA DE AHORRO CON BANCO FALABELLA S A?",
                        ordenPregunta = "2",
                        respuestas = listOf(
                            OpcionRespuesta("001", "0 A 1 AÑOS"),
                            OpcionRespuesta("002", "2 A 5 AÑOS"),
                            OpcionRespuesta("003", "6 A 10 AÑOS"),
                            OpcionRespuesta("004", "11 A 15 AÑOS"),
                            OpcionRespuesta("005", "16 AÑOS O MAS"),
                            OpcionRespuesta("006", "NO TENGO CUENTA DE AHORRO CON LA ENTIDAD")
                        )
                    ),
                    Pregunta(
                        idPregunta = "a0RO4000004jO2LMAU",
                        pregunta = "HACE CUANTO TIEMPO TIENE USTED UN/UNA PLAN / PAQUETE TELEFONIA CELULAR  CON TIGO - COLOMBIA MOVIL S.A. E.S.P",
                        ordenPregunta = "3",
                        respuestas = listOf(
                            OpcionRespuesta("001", "ENTRE 0 Y 3 AÑOS"),
                            OpcionRespuesta("002", "ENTRE 4 Y 5 AÑOS"),
                            OpcionRespuesta("003", "ENTRE 6 Y 7 AÑOS"),
                            OpcionRespuesta("004", "ENTRE 8 Y 11 AÑOS"),
                            OpcionRespuesta("005", "12 AÑOS O MAS"),
                            OpcionRespuesta("006", "NO TENGO PLAN / PAQUETE TELEFONIA CELULAR CON LA ENTIDAD")
                        )
                    ),
                    Pregunta(
                        idPregunta = "a0RO4000004jO2MMAU",
                        pregunta = "EL VALOR DE LA CUOTA DE FEBRERO DE 2025 DE SU CREDITO DE VEHICULOS O FACTORING CON BANCO DAVIVIENDA S.A. / DAVIPLATA ESTABA ENTRE:",
                        ordenPregunta = "4",
                        respuestas = listOf(
                            OpcionRespuesta("001", "\$110,001 Y \$330,000"),
                            OpcionRespuesta("002", "\$330,001 Y \$550,000"),
                            OpcionRespuesta("003", "\$550,001 Y \$770,000"),
                            OpcionRespuesta("004", "\$770,001 Y \$990,000"),
                            OpcionRespuesta("005", "\$990,001 Y \$1,210,000"),
                            OpcionRespuesta("006", "NO TENGO CREDITO DE VEHICULOS O FACTORING CON LA ENTIDAD")
                        )
                    )
                )
            )*/

            Log.d("PreguntasFragment", "Questions response: ${questionsResponse}")

            if(questionsResponse != null && questionsResponse.codigo == "200" && questionsResponse.preguntas.isNotEmpty()){
                //val resonsesList = questionsResponse

                // Inicializar el adapter con callback
                val preguntas = questionsResponse.preguntas
                preguntaAdapter = PreguntaAdapter(preguntas) { preguntaId, respuesta ->
                    //respuestasSeleccionadas[preguntaId.toInt()] = respuesta
                    respuestasSeleccionadas[preguntaId] = respuesta
                    Log.d("PreguntasFragment", "Pregunta $preguntaId: $respuesta")
                }

                // Configurar RecyclerView
                binding.recyclerPreguntas.apply {
                    layoutManager = LinearLayoutManager(requireContext()).apply {
                        orientation = LinearLayoutManager.VERTICAL
                    }
                    adapter = preguntaAdapter
                    setHasFixedSize(true)

                    // Opcional: añadir decoración para separación entre items
                    addItemDecoration(
                        DividerItemDecoration(
                            context,
                            LinearLayoutManager.VERTICAL
                        )
                    )
                }

                // Configurar botón de enviar
                binding.btnEnviar.setOnClickListener {
                    validarYEnviarRespuestas(preguntas)
                }
            }else{
                // Error al obtener preguntas
                gotoRejectedScreen()
            }
        }
    }
}