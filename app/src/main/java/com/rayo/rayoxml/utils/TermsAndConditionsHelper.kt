package com.rayo.rayoxml.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.co.models.PaymentDate
import com.rayo.rayoxml.co.services.Loan.LoanRepository
import com.rayo.rayoxml.co.services.Loan.LoanStepFivePlusLoadResponse
import com.rayo.rayoxml.co.services.Loan.LoanValidationStepViewModel
import com.rayo.rayoxml.co.services.Loan.SolicitudPlpStep1
import com.rayo.rayoxml.co.services.Loan.SolicitudStepFiveLoad
import com.rayo.rayoxml.co.services.User.Solicitud
import com.rayo.rayoxml.co.services.User.UserRepository
import com.rayo.rayoxml.co.ui.loading.LoadingDialogFragment
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class TermsAndConditionsHelper(private val context: Context, private val container: ViewGroup,
                               private val rootView: View) {

    private lateinit var repository: LoanRepository
    private lateinit var validationViewModel: LoanValidationStepViewModel
    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment
    private val SUCCESSFUL_FORM_RESULT: String = "La solicitud fue procesada exitosamente"

    var loanType = ""
    private var formId: String? = null

    private lateinit var userRepository: UserRepository
    private var loanData: SolicitudStepFiveLoad? = null
    private var loanDataPlp: SolicitudPlpStep1? = null

    private var paymentDates: MutableList<PaymentDate>? = null
    private var paymentAmounts: MutableList<String>? = null

    private var nombre: String? = null
    private var apellido: String? = null
    private var direccion: String? = null
    private var telefono: String? = null
    private var correo: String? = null
    private var cedula: String? = null
    private var codigoSeguridad: String? = null
    private var registroValidacion: String? = null
    private var idTransaccion: String? = null

    private val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat

    private var loanRpRenewalInfoLoadResponse: LoanStepFivePlusLoadResponse? = null
    //private var userData: Solicitud? = null

    private var isLoanTypeLoaded = false
    private var isFormIdLoaded = false
    private var isLoanPlpDataLoaded = false
    private var isPaymentDatesLoaded = false
    private var isPaymentAmountsLoaded = false
    private var isUserDataLoaded = false
    private var hasSubmitted = false

    fun setupTermsAndConditions(loanDataPlp: SolicitudPlpStep1? = null, loanData: Solicitud? = null) {

        if(loanDataPlp != null){
            //println("Datos términos: ${loanDataPlp}")
            nombre = loanDataPlp.nombre
            apellido = loanDataPlp.apellido
            direccion = ""
            telefono = loanDataPlp.celular
            correo = loanDataPlp.correo
            cedula = loanDataPlp.numeroDocumento
            codigoSeguridad = ""
            registroValidacion = ""
            idTransaccion = ""
            if(!loanRpRenewalInfoLoadResponse?.solicitud?.formulario.isNullOrEmpty()){
                val loanData = loanRpRenewalInfoLoadResponse?.solicitud
                codigoSeguridad = loanData?.codigoSeguridad
                registroValidacion = loanData?.registroValidacion
                idTransaccion = loanData?.idTransaccion
            }
        }else if(loanData != null){
            //println("Datos términos: ${loanData}")
            nombre = loanData.nombre
            apellido = loanData.apellidos
            direccion = ""
            telefono = loanData.celular
            correo = loanData.email
            cedula = loanData.documento
            codigoSeguridad = ""
            registroValidacion = ""
            idTransaccion = ""
        }else{
            // Cargar sin datos
            //println("Datos términos plantilla")
            nombre = "el actual usuario"
            apellido = ""
            direccion = ""
            telefono = ""
            correo = ""
            cedula = ""
            codigoSeguridad = ""
            registroValidacion = ""
            idTransaccion = ""
        }

        // Limpia el contenedor por si acaso
        container.removeAllViews()

        // Agrega cada cláusula
        addClause0()
        addClause1()
        addClause2()
        addClause3()
        addClause4()
        addClause5()
        addClause6()
        addClause7()
        addClause8()
        addClause9()
        addClause10()
        addClause11()
        addClause12()
        addClause13()
        addClause14()
        addClause15()
        addClause16()
        addClause17()
        addClause18()
        addClause19()
        addClause20()
        addClause21()
        addClause22()

        setupScrollButtons(rootView)
    }

    private fun setupScrollButtons(rootView: View) {
        val scrollView = this.rootView.findViewById<ScrollView>(R.id.scrollView)
        val buttonScrollUp = this.rootView.findViewById<ImageButton>(R.id.buttonScrollUp)
        val buttonScrollDown = this.rootView.findViewById<ImageButton>(R.id.buttonScrollDown)

        val scrollSpace = 1000

        Log.d("TermsAndConditionsHelper", "setupScrollButtons")
        buttonScrollDown.setOnClickListener {
            Log.d("TermsAndConditionsHelper", "⬇ Scroll Down Clicked")
            scrollView?.post {
                scrollView.smoothScrollBy(0, scrollSpace)
            }
        }

        buttonScrollUp.setOnClickListener {
            Log.d("TermsAndConditionsHelper", "⬆ Scroll Up Clicked")
            scrollView?.post {
                scrollView.smoothScrollBy(0, -scrollSpace)
            }
        }

        /*val screenHeight = context.resources.displayMetrics.heightPixels
        val targetY = screenHeight / 3f
        buttonScrollDown.y = targetY*/
    }

    private fun addClause0() {
        var text = "RAYO y ${nombre} ${apellido}, quien aparece relacionado en la correspondiente solicitud de crédito, en adelante denominado como el CLIENTE, por medio del presente documento manifiestan su intención de celebrar el presente contrato de apertura de crédito, relación que se encontrará regida por las normas pertinentes del Código de Comercio, Estatuto del Consumidor Ley 1480 de 2011 y demás normas concordantes sobre la materia y especialmente por las siguientes claúsulas: "
        addTextWithHighlights(text, listOf("RAYO", "CLIENTE"))

        addBoldText("")
        text = "1. Información Completa del Acreedor"
        addBoldText(text)

        text = "Acreedor: RAYOCOL S.A.S."
        addTextWithHighlights(text, listOf("Acreedor"))

        text = "NIT: 901287068-0"
        addTextWithHighlights(text, listOf("NIT"))

        text = "Dirección: Carrera 11a, #94 A - 23, oficina 106, Bogotá, Colombia"
        addTextWithHighlights(text, listOf("Dirección"))

        text = "Whastapp: 322 4060064"
        addTextWithHighlights(text, listOf("Whastapp"))

        text = "2. Información Completa del Deudor"
        addBoldText(text)

        text = "Nombre CLIENTE: ${nombre} ${apellido}"
        addTextWithHighlights(text, listOf("Nombre CLIENTE"))

        text = "Dirección: ${direccion}"
        addTextWithHighlights(text, listOf("Dirección"))

        text = "Teléfono: ${telefono}"
        addTextWithHighlights(text, listOf("Teléfono"))

        text = "Correo Electrónico: ${correo}"
        addTextWithHighlights(text, listOf("Correo Electrónico"))

        // Montos
        var subtitles = listOf("Valor solicitado", "Interés", "Tecnología", "IVA", "Fianza + IVA", "Descuento", "Total a pagar", "Total a desembolsar", "Total a pagar después del desembolso ")
        var amountRows = subtitles.zip(paymentAmounts ?: emptyList()).map { (subtitle, amount) ->
            listOf(subtitle, "$amount")
        }
        if (amountRows != null && amountRows.size > 0) {
            var amountHeaders = listOf("ITEMS", "MONTOS")
            addTable(amountHeaders, amountRows)
        }

        val headers = listOf("FECHA DE ABONO", "MONTO")
        val rows = paymentDates?.map { item ->
            listOf(
                "${item.date}",
                "$${formatter.format(item.amount)}"
            )
        }
        if (rows != null) {
            addTitle("PAGOS")
            addTable(headers, rows)
        }
    }

    private fun addClause1() {
        // Título
        addTitle("CLÁUSULA PRIMERA. Objeto:")

        // Contenido
        val text = "La prestación del servicio surge en virtud de la solicitud realizada por el CLIENTE, en virtud de la cual RAYO accedió a desembolsar al la suma de dinero indicada en su solicitud dentro de los 4 días siguientes a la realización de la misma y el CLIENTE se compromete a restituir incondicionalmente las sumas de dinero por concepto de capital e intereses en los términos pactados."
        addTextWithHighlights(text, listOf("CLIENTE", "RAYO"))
    }

    private fun addClause2() {
        // Título
        addTitle("CLÁUSULA SEGUNDA. Obligaciones de RAYO:")

        // Contenido
        val text = "Entregar al la suma de dinero previamente aprobada de acuerdo con sus políticas de crédito, lo cual será informado al , así como todas aquellas contempladas en el presente contrato. RAYO realizará, en cada caso puntual, un análisis de viabilidad de crédito, pudiendo incluso llegar a ofrecer créditos por valor superior o inferior al solicitado por el CLIENTE. En todo caso, la decisión de realizar el crédito es a entera discreción de RAYO, pudiendo negar el crédito en cualquier momento sin estar obligado a manifestar justificación alguna."
        addTextWithHighlights(text, listOf("RAYO", "CLIENTE"))
    }

    private fun addClause3() {
        // Título
        addTitle("CLÁUSULA TERCERA. Obligaciones Del CLIENTE:")

        // Lista numerada
        val items = listOf(
            "Realizar los pagos conforme a lo acordado con RAYO en el presente contrato.",
            "Actualizar por lo menos una vez al año la información por él suministrada y los demás documentos exigidos para la aprobación del crédito, de acuerdo con lo estipulado en este contrato y a cumplir las demás obligaciones contempladas en él, en la Ley 1480 de 2011 y su reglamentación o cualquier norma que la modifique o adicione.",
            "Revisar periódicamente el contrato, puesto por RAYO a su disposición en la página web de RAYO."
        )
        addNumberedList(items, listOf("RAYO", "CLIENTE"))
    }

    private fun addClause4() {
        // Título
        addTitle("CLÁUSULA CUARTA. Especificaciones:")

        // Primer párrafo
        var text = "El servicio de préstamo de dinero proporcionado por RAYO es un servicio por el cual RAYO se compromete a hacer entrega una suma determinada de dinero en moneda legal colombiana, a un plazo fijo de financiación previamente establecido con el . Una vez el crédito haya sido aprobado, RAYO desembolsará el valor total de este en la cuenta bancaria indicada por el en la solicitud de crédito. Dicho desembolso estará sujeto a la disponibilidad de fondos de RAYO, así como al mantenimiento de las circunstancias que hayan sido determinantes para la aprobación del crédito."
        addTextWithHighlights(text, listOf("RAYO"))

        // Segundo párrafo
        text = "En cada caso y antes de la aceptación, será informada a través de la página web de RAYO o a través de correo electrónico, las condiciones específicas del crédito solicitado dentro de las cuales se enmarcarán dentro del cupo de crédito que, podrá ser otorgado hasta por un máximo de \$200,000.00, por el término que se indique al momento de realizar la solicitud."
        addTextWithHighlights(text, listOf("RAYO"))

        // Tercer párrafo
        text = "Dicho cupo de crédito será asignado conforme a las políticas de riesgo de RAYO y podrá ser usado por el en una o varias transacciones hasta la fecha de vencimiento o cualquiera de las prórrogas automáticas que sean otorgadas de manera unilateral por períodos iguales al inicialmente pactado."
        addTextWithHighlights(text, listOf("RAYO"))

        // Párrafo primero
        addSubtitle("PARÁGRAFO PRIMERO:")
        text = "El valor del cupo de crédito otorgado podrá ser modificado por RAYO en cualquier momento, de acuerdo con nuestras políticas de riesgo."
        addTextWithHighlights(text, listOf("RAYO"))

        // Párrafo segundo
        addSubtitle("PARÁGRAFO SEGUNDO:")
        text = "Una vez el haya completado su cupo máximo de crédito, RAYO podrá ofrecer créditos adicionales, los cuales se encontrarán sujetos a una tecnología de mayor valor respecto de los créditos otorgados dentro del cupo de crédito. El monto de la variación de estos valores se encontrará sujeto a las condiciones particulares del cliente y se le informará antes de otorgar el crédito."
        addTextWithHighlights(text, listOf("RAYO"))
    }

    private fun addClause5() {
        // Título
        addTitle("CLÁUSULA QUINTA. Intereses:")

        // Primer párrafo
        var text = "El CLIENTE se obliga a reconocer a RAYO intereses remuneratorios sobre el capital, además de los intereses de mora cuando haya lugar a ellos. La tasa de interés remuneratorio será equivalente a 26.39% Efectiva Anual, y la tasa de interés de mora corresponderá a 26.39% Efectiva Anual. En la eventualidad de que cualquiera de estas tasas llegase a, por las fluctuaciones del mercado, superar los máximos legales permitidos, se entenderá que la tasa de interés será la máxima legal."
        addTextWithHighlights(text, listOf("CLIENTE", "RAYO"))

        // Segundo párrafo
        text = "Ahora bien, en caso de que la tasa de interés moratorio haya aumentado de acuerdo con lo establecido por la Superintendencia Financiera de Colombia para la fecha de entrar en mora, y mientras la mora se mantenga para los periodos subsiguientes, se entenderá que la tasa aplicable será la máxima permitida en Colombia para la fecha de su liquidación."
        addTextWithHighlights(text, listOf())
    }

    private fun addClause6() {
        // Título
        addTitle("CLÁUSULA SEXTA. Cargos:")

        // Primer párrafo
        var text = "Adicional a los intereses remuneratorios y moratorios cuando haya lugar, el se obliga a cancelarte a RAYO los siguientes cargos:"
        addTextWithHighlights(text, listOf("RAYO"))

        // Subtítulo CARGO TECNOLÓGICO
        addSubtitle("CARGO TECNOLÓGICO:")
        text = "Corresponde al uso de un desarrollo tecnológico de liquidación de crédito, cual es usado a la finalización del crédito. Por tal motivo, el CLIENTE deberá pagar por concepto de CARGO TECNOLÓGICO un valor de \$101,750.00, monto que será cobrado solamente en el momento en que el utilice los servicios de RAYO."
        addTextWithHighlights(text, listOf("CLIENTE", "CARGO TECNOLÓGICO", "RAYO"))

        // Párrafo primero
        addSubtitle("PARÁGRAFO PRIMERO:")
        text = "El cobro del cargo mencionado con anterioridad dependerá del tipo de crédito solicitado por cada uno de los clientes, siendo cobrado primero el capital y los intereses para luego ser cobrado el cargo descrito en la presente cláusula y que deberán ser pagados por los clientes íntegramente en el momento que se realice el cobro so pena de la pérdida de los beneficios otorgados por RAYO."
        addTextWithHighlights(text, listOf("RAYO"))
    }

    private fun addClause7() {
        // Título
        addTitle("CLÁUSULA SÉPTIMA. Mora:")

        // Contenido
        val text = "Si dentro de los términos y condiciones pactados el no cancela el valor de la cuota, se causarán intereses de mora sobre el saldo de capital, los cuales serán liquidados por RAYO a la tasa máxima de usura legal permitida certificada por la autoridad competente."
        addTextWithHighlights(text, listOf("RAYO"))
    }

    private fun addClause8() {
        // Título
        addTitle("CLÁUSULA OCTAVA. Gastos de Cobranza:")

        // Primer párrafo
        var text = "Es el cobro que se genera en cabeza del CLIENTE en razón a las gestiones y actividades desplegadas por RAYO encaminadas a la recuperación de cartera."
        addTextWithHighlights(text, listOf("CLIENTE", "RAYO"))

        // Segundo párrafo
        text = "En este proceso contactamos al deudor, deudor solidario, avalistas, y garantes (RAYO no contacta en ningún momento a las referencias personales o de otra índole para la realización de actividades de cobranza), que presentan dificultad para efectuar el pago de las obligaciones que están en mora, con el fin de normalizar el pago de la obligación."
        addTextWithHighlights(text, listOf("RAYO"))

        // Párrafo primero
        addSubtitle("PARÁGRAFO PRIMERO:")
        text = "Para todos los efectos, la gestión de cobranza adelantada por RAYO se efectuará de la siguiente manera:"
        addTextWithHighlights(text, listOf("RAYO"))

        // Subtítulo Cobranza Pre-jurídica
        addBoldText("Cobranza Pre-jurídica:")
        text = "Es la cobranza que se realiza desde el día 15 de mora hasta los 359 días de mora, y es gestionada por RAYO, a través de su servicio denominado CANTILLANO. Los gastos que se generen por la Gestión de Cobranza Pre-jurídica deben ser asumidos por el CLIENTE."
        addTextWithHighlights(text, listOf("RAYO", "CLIENTE"))

        // Subtítulo Cobranza Jurídica
        addBoldText("Cobranza Jurídica:")
        text = "Es la cobranza que se realiza a partir del día 360 de mora. En este tipo de cobranza, RAYO radica ante el abogado externo o casa de cobranza los documentos necesarios que garanticen la posible admisión de una demanda ejecutiva y/o trámite para el pago directo."
        addTextWithHighlights(text, listOf("RAYO"))

        // Párrafo segundo
        addSubtitle("PARÁGRAFO SEGUNDO:")
        text = "Los canales que dispone RAYO para la gestión de cobranza:"
        addTextWithHighlights(text, listOf("RAYO"))

        // Lista no numerada
        val items = listOf(
            "Contacto telefónico",
            "Redes Sociales - WhatsApp",
            "Mensajes de texto",
            "Correo electrónico",
            "Correo masivo o personalizado",
            "Visitas personales"
        )
        addBulletList(items)

        // Texto final
        text = "RAYO solo contactará al CLIENTE mediante los canales que autorice para tal efecto."
        addTextWithHighlights(text, listOf("RAYO", "CLIENTE"))

        // Párrafo tercero
        addSubtitle("PARÁGRAFO TERCERO:")
        text = "El horario en el cual RAYO se comunicará con el CLIENTE es el siguiente:"
        addTextWithHighlights(text, listOf("RAYO", "CLIENTE"))

        // Lista de horarios
        val scheduleItems = listOf(
            "De lunes a viernes de 07:00 a.m. a 07:00 p.m.",
            "Sábados de 08:00 a.m. a 03:00 p.m."
        )
        addBulletList(scheduleItems)

        // Texto final
        text = "RAYO no realizará gestión de cobranza los domingos y días festivos no por fuera de los horarios aquí establecidos, excepto que, por requerimiento del CLIENTE, en un documento diferente solicite que RAYO efectué la gestión de cobranza tales días."
        addTextWithHighlights(text, listOf("RAYO", "CLIENTE"))
    }

    private fun addClause9() {
        // Título
        addTitle("CLÁUSULA NOVENA. Beneficios:")

        // Contenido
        val text = "El obtendrá múltiples beneficios y descuentos según el estatus que ostente el , el cual conste en el sitio web de la empresa."
        addTextWithHighlights(text, listOf())
    }

    private fun addClause10() {
        // Título
        addTitle("CLÁUSULA DÉCIMA. Autorización Débito Automático:")

        // Primer párrafo
        var text = "El autoriza de forma irrevocable a RAYO para que el día de pago establecido por las partes se debite de su cuenta bancaria el valor total de las obligaciones mutuas exigibles que tenga con RAYO; si el es titular de una o varias cuentas de ahorro, corrientes o de cualquier otra cuenta o depósito, RAYO podrá acreditar o debitar dicho importe en cualquiera de ellas, o fraccionarlo entre las mismas, a su elección."
        addTextWithHighlights(text, listOf("RAYO"))

        // Segundo párrafo
        text = "Igualmente, el autoriza a que RAYO realice el débito automático de las sumas que se causen por concepto de intereses de mora, gastos de cobranza y demás costos que puedan surgir en caso de que no existan fondos suficientes en la cuenta o cuentas reportadas el día de la realización del débito aquí autorizado para cubrir la totalidad de las obligaciones."
        addTextWithHighlights(text, listOf("RAYO"))

        // Tercer párrafo
        text = "El CLIENTE reconoce que en RAYO utilizamos DRUO para conectarnos con su cuenta bancaria y procesar transacciones desde y las cuentas que conecte. Para procesar estas transacciones declaro y garantizo que: (i) proporciono información precisa y completa sobre mí, (ii) autorizo a compartir dicha información con DRUO para la prestación de los servicios, (iii) autorizo a generar consultas y cobros a las cuentas que conecte a nosotros por medio de DRUO según las obligaciones descritas en este acuerdo. Al aceptar este acuerdo, acepto el tratamiento de datos de acuerdo con la Política de Privacidad de DRUO al igual que el Acuerdo de Usuarios Finales de DRUO."
        addTextWithHighlights(text, listOf("CLIENTE", "RAYO"))

        // Párrafo primero
        addSubtitle("PARÁGRAFO PRIMERO:")
        text = "En aquellos casos cuando la fecha de causación corresponda a un día no hábil, EL CLIENTE expresamente autoriza a RAYO a que se realice el débito del que habla la presente cláusula el día hábil inmediatamente anterior a la fecha de causación. Cuando se presente la situación en mención, RAYO realizará las correspondientes deducciones por los días de intereses y demás cargos no causados."
        addTextWithHighlights(text, listOf("CLIENTE", "RAYO"))
    }

    private fun addClause11() {
        // Título
        addTitle("CLÁUSULA DÉCIMA PRIMERA. Aceleración:")

        // Primer párrafo
        var text = "RAYO podrá declarar extinguido o insubsistente el plazo del crédito y exigir su inmediata cancelación con todos sus accesorios, sin necesidad de requerimiento judicial o extrajudicial para constituir en mora, en cualquiera de los siguientes casos:"
        addTextWithHighlights(text, listOf("RAYO"))

        // Lista numerada
        val items = listOf(
            "Mora de uno de los pagos pactados",
            "Incumplimiento por parte del de alguna de sus obligaciones relacionadas con el crédito",
            "Incumplimiento de cualquiera de las obligaciones emanadas de las garantías",
            "Cuando no sea posible verificar la información suministrada por el"
        )
        addNumberedList(items, listOf())

        // Párrafo primero
        addSubtitle("PARÁGRAFO PRIMERO:")
        text = "En el evento en que RAYO declarare extinguido o insubsistente el plazo del crédito, informará previamente esta situación al , según los casos establecidos en la normatividad vigente."
        addTextWithHighlights(text, listOf("RAYO"))

        // Párrafo segundo
        addSubtitle("PARÁGRAFO SEGUNDO:")
        text = "Cualquier incumplimiento o retraso en el pago, aún de una cuota cuando se trate de varias, permitirá a RAYO realizar el cobro judicial o extrajudicial de la totalidad de la suma debida, acelerando por tanto todas las cuotas futuras, así como reclamar, si lo desea, todos los perjuicios que el incumplimiento le genere."
        addTextWithHighlights(text, listOf("RAYO"))
    }

    private fun addClause12() {
        // Título
        addTitle("CLÁUSULA DÉCIMA SEGUNDA. Perdida de beneficios:")

        // Contenido
        val text = "Una vez el entra en mora RAYO unilateralmente podrá disminuir los beneficios otorgados o incluso suprimirlos."
        addTextWithHighlights(text, listOf("RAYO"))
    }

    private fun addClause13() {
        // Título
        addTitle("CLÁUSULA DÉCIMA TERCERA. AUTORIZACIÓN TRATAMIENTO DE DATOS PERSONALES")

        // Primer párrafo
        addBoldText("Centrales de información de riesgo crediticio:")
        var text = "En caso de no realizar el pago a más tardar 20 días calendario después haber sido informado sobre el incumplimiento, el CLIENTE autoriza a RAYO para reportar la mora de su obligación a las centrales de riesgo crediticio, sin perjuicio de las demás acciones que RAYO pueda adelantar para el cobro de las obligaciones a su favor. Para efectos de la comunicación de aviso de reporte a las centrales de riesgo, la misma será remitida de manera escrita mediante comunicación dirigida al domicilio registrado por el CLIENTE, o por cualquier tipo de mensaje de datos incluyendo correo electrónico, mensajes de texto, plataformas de mensajería instantánea, o a través de cualquier medio del que quede registro para posteriores consultas. De igual forma el CLIENTE autoriza a RAYO y a quien en el futuro ostente la calidad de acreedor para:"
        addTextWithHighlights(text, listOf("CLIENTE", "RAYO"))

        // Lista numerada principal
        val mainItems = listOf(
            "Reportar con fines estadísticos, de control, supervisión, evaluación de riesgos y demás que señalen las políticas de RAYO y/o las normas vigentes colombianas, los datos personales del CLIENTE (incluyendo financieros y comerciales) y la información relacionada con el surgimiento de cualquier obligación, novedad, modificación, extinción, cumplimiento o incumplimiento de las obligaciones contraídas o que el CLIENTE llegue a contraer en favor de RAYO y",
            "Reportar cualquier otro dato que se estime pertinente con relación a la existencia de deudas vencidas sin cancelar o con la utilización indebida de los productos y/o servicios prestados u ofrecidos por RAYO",
            "Solicitar y consultar a los operadores de información los datos (incluyendo financieros y comerciales) con el fin de:"
        )
        addNumberedList(mainItems, listOf("CLIENTE", "RAYO"))

        // Sub-lista para el tercer item
        val subItems = listOf(
            "Obtener información referente a las relaciones comerciales establecidas con cualquier entidad financiera o comercial y",
            "Confirmar datos para iniciar o mantener una relación contractual"
        )

        // Añadir sangría para la sub-lista
        val subListContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dpToPx(40), 0, 0, 0) // Más sangría para sub-lista
            }
        }

        subItems.forEachIndexed { index, item ->
            val textView = TextView(context).apply {
                text = "${index + 1}. $item"
                setTextColor(Color.parseColor("#262626"))
                textSize = 14f
                setLineSpacing(0f, 1.6f)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(8))
                }
            }
            subListContainer.addView(textView)
        }

        container.addView(subListContainer)
    }

    private fun addClause14() {
        // Título
        addTitle("CLÁUSULA DÉCIMA CUARTA. Autorización uso de datos:")

        // Primer párrafo
        var text = "El CLIENTE de manera voluntaria, previa, expresa, e informada autoriza el tratamiento de sus datos personales a RAYOCOL S.A.S. (en adelante \"RAYO\") y a quien ostente sus derechos para que recolecte, capture, registre, use, circule, actualice, procese, almacene, o suprima los mismos para los siguientes propósitos: (i) con fines contractuales, (ii) Comercializar y promocionar sus servicios, (iii) para actualizar datos y brindar información relevante, (iv) para asegurar y prestar el servicio de atención al USUARIO, (v) para la gestión de cobranza, la cual se adelantará directamente o a través de terceros por medio de los canales de comunicación tanto físicos como remotos, como carta o comunicación escrita a la dirección que ha registrado, llamadas telefónicas, email, redes sociales tales como Facebook, o mensajes de texto (a través de SMS, sitio web o aplicaciones móviles), WhatsApp u otras plataformas de mensajería instantánea, al número de celular que ha registrado o cualquier otro dato de contacto que haya informado, con el objeto de hacerle llegar información relacionada con las finalidades descritas, a menos que el CLIENTE indique no autoriza alguno de los canales de comunicación establecidos, lo cual podrá hacer a través de documento que se considerará anexo al presente contrato. (vi) para transferir o transmitir los datos personales a proveedores, comercios, empresas y/o entidades afiliadas o aliados comerciales con los que se haya pactado un acuerdo de transferencia de datos, ya sean transferidos en territorio colombiano o en el extranjero."
        addTextWithHighlights(text, listOf("CLIENTE", "RAYO"))

        // Segundo párrafo
        text = "El CLIENTE entiende que es de carácter facultativo suministrar información que verse sobre Datos Sensibles, entendidos como aquellos que afectan la intimidad tales como raza, etnia, orientación sexual, estado de salud y existencia de algún tipo de discapacidad; o sobre menores de edad, y que el no suministro de los mismos no podrá considerarse como condicionante para realizar alguna clase de actividad en la que me encuentre involucrado."
        addTextWithHighlights(text, listOf("CLIENTE"))

        // Tercer párrafo
        text = "Igualmente, el CLIENTE declara que cuenta con la autorización de los terceros que registra como codeudor, deudor solidario, avalista y garante para que sus datos personales sean tratados bajo las mismas finalidades autorizadas en el presente documento."
        addTextWithHighlights(text, listOf("CLIENTE"))

        // Cuarto párrafo
        text = "Aunado lo anterior, autoriza a RAYO o a quien represente sus derechos, a acceder a sus datos personales contenidos en la base de datos de Operadores de información de seguridad social autorizados por el Ministerio de Salud y Protección Social o de administradoras de pensiones, a sus datos personales recolectados por medio del presente formulario, y a sus datos personales contenidos en la base de datos de los operadores de información (centrales de riesgo), para finalidades de gestión de riesgo crediticio tales como: (i) elaboración y circulación a terceros de scores crediticios, herramientas de validación de ingresos, herramientas predictivas de ingresos, herramientas para evitar el fraude y en general, herramientas que permitan adelantar una adecuada gestión del riesgo crediticio. (ii) Compararla, contrastarla y complementarla con la información financiera, comercial, crediticio, de servicios y proveniente de terceros países de Experian."
        addTextWithHighlights(text, listOf("RAYO"))
    }

    private fun addClause15() {
        // Título
        addTitle("CLÁUSULA DÉCIMA QUINTA. Cobranza Pre-jurídica y Jurídica:")

        // Contenido
        val text = "En caso de cobro prejudicial o judicial, estarán a cargo del CLIENTE todos los gastos originados en el proceso de cobranza y honorarios que se causen para su recaudo, de acuerdo con las tarifas establecidas por RAYO, las cuales se informarán por los medios y canales que se establezcan para el efecto.\n"
        /*+"Días de Mora \tGastos de Cobranza\n" +
        "60 días a 90 días \t\$95.000\n" +
        "90 días a 120 días \t\$110.000\n" +
        "Más de 120 días \t\$120.000\n"*/
        addTextWithHighlights(text, listOf("CLIENTE", "RAYO", "Días de Mora","Gastos de Cobranza"))

        val headers = listOf("Días de Mora", "Gastos de Cobranza")
        val rows = listOf(
            listOf("60 días a 90 días", "$95.000"),
            listOf("90 días a 120 días", "$110.000"),
            listOf("Más de 120 días", "$120.000")
        )
        if (rows != null) {
            addTable(headers, rows)
        }
    }

    private fun addClause16() {
        // Título
        addTitle("CLÁUSULA DÉCIMA SEXTA. Modificación del Contrato:")

        // Contenido
        val text = "RAYO se reserva el derecho de modificar, limitar, suprimir o adicionar los términos y condiciones de este contrato en cualquier momento. Las modificaciones se informarán mediante publicación en la página de internet de RAYO o por cualquier otro medio eficaz. Si pasados 15 días hábiles el CLIENTE no se retira ni manifiesta inconformidad, se entenderá como aceptación."
        addTextWithHighlights(text, listOf("RAYO", "CLIENTE"))
    }

    private fun addClause17() {
        // Título
        addTitle("CLÁUSULA DÉCIMA SÉPTIMA. Duración y Terminación del Contrato:")

        // Contenido
        val text = "El contrato durará 12 (DOCE) meses desde la vigencia del crédito. RAYO podrá darlo por terminado si no se verifica la información o si el CLIENTE no actualiza anualmente sus datos. Se notificará por la última dirección o correo registrado. Pasados 15 días hábiles, se entenderá terminado."
        addTextWithHighlights(text, listOf("RAYO", "CLIENTE"))
    }

    private fun addClause18() {
        // Título
        addTitle("CLÁUSULA DÉCIMA OCTAVA:")

        // Contenido
        val text = "El CLIENTE autoriza a RAYO a compartir información publicada por él relacionada con los servicios, siempre que tenga las autorizaciones cuando se trate de terceros."
        addTextWithHighlights(text, listOf("CLIENTE", "RAYO"))
    }

    private fun addClause19() {
        // Título
        addTitle("CLÁUSULA DÉCIMA NOVENA:")

        // Contenido
        val text = "El CLIENTE acepta el cobro por el servicio de fianza subsidiaria prestado por CONFÈ. En caso de incumplimiento, CONFÈ podrá subrogarse y cobrar intereses de mora y gastos. El pago que realice CONFÈ no extingue la obligación."
        addTextWithHighlights(text, listOf("CLIENTE", "CONFÈ"))
    }

    private fun addClause20() {
        // Título
        addTitle("CLÁUSULA VIGÉSIMA:")

        // Contenido
        val text = "El CLIENTE acepta que el incumplimiento faculta a RAYO para iniciar acciones legales. Este documento y el estado de cuenta serán título ejecutivo."
        addTextWithHighlights(text, listOf("CLIENTE", "RAYO"))
    }

    private fun addClause21() {
        // Título
        addTitle("CLÁUSULA VIGÉSIMA PRIMERA:")

        // Primer párrafo
        var text = "El CLIENTE puede retractarse dentro de los cinco (5) días hábiles tras la aprobación del crédito. Si lo hace, deberá devolver el dinero recibido en las mismas condiciones y medios, sin retenciones."
        addTextWithHighlights(text, listOf("CLIENTE"))

        // Subtítulo
        addBoldText("Se exceptúan del derecho de retracto:")

        // Lista no numerada
        val items = listOf(
            "1. Servicios iniciados con acuerdo del consumidor",
            "2. Bienes o servicios sujetos a fluctuaciones del mercado financiero",
            "3. Bienes confeccionados o personalizados",
            "4. Bienes que no puedan devolverse o se deterioren fácilmente",
            "5. Servicios de apuestas y loterías",
            "6. Bienes perecederos",
            "7. Bienes de uso personal"
        )
        addBulletList(items)

        // Último párrafo
        text = "El CLIENTE deberá devolver la totalidad del dinero a RAYO. Para retractarse, debe escribir a info@rayo.com.co manifestando su deseo."
        addTextWithHighlights(text, listOf("CLIENTE", "RAYO"))
    }

    private fun addClause22() {
        // Título
        addTitle("CLÁUSULA VIGÉSIMA SEGUNDA:")

        // Primer párrafo
        var text = "El presente documento es de público conocimiento y es informado al CLIENTE con antelación y posterioridad a la realización de una solicitud de crédito y al eventual desembolso. La aceptación del crédito supone la aceptación íntegra a todas las partes del presente documento. El CLIENTE manifiesta que su aceptación del crédito es suficiente exteriorización de su consentimiento, cual es dado de forma libre y voluntaria, por lo que el CLIENTE acepta y reconoce estar obligado a cumplir lo establecido en el presente contrato. Este documento se genera bajo la aprobación del CLIENTE, según:"
        addTextWithHighlights(text, listOf("CLÁUSULA VIGÉSIMA SEGUNDA.","CLIENTE"))

        // Información adicional
        val items = listOf(
            "1. Código de seguridad: ${codigoSeguridad}",
            "2. Registro validación: ${registroValidacion}",
            "3. ID transacción: ${idTransaccion}",
        )
        addBulletList(items)
    }

    private fun addTitle(title: String) {
        val textView = TextView(context).apply {
            text = title
            setTextColor(Color.parseColor("#171717"))
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(24), 0, dpToPx(12))
            }
        }
        container.addView(textView)
    }

    private fun addSubtitle(subtitle: String) {
        val textView = TextView(context).apply {
            text = subtitle
            setTextColor(Color.parseColor("#171717"))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(12), 0, 0)
            }
        }
        container.addView(textView)
    }

    private fun addTextWithHighlights(text: String, highlightedWords: List<String>) {
        val textView = TextView(context).apply {
            setText(formatTextWithHighlights(text, highlightedWords))
            setTextColor(Color.parseColor("#262626"))
            textSize = 14f
            setLineSpacing(0f, 1.6f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }
        }
        container.addView(textView)
    }

    private fun addNumberedList(items: List<String>, highlightedWords: List<String>) {
        val listContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dpToPx(20), dpToPx(8), 0, 0)
            }
        }

        items.forEachIndexed { index, item ->
            val textView = TextView(context).apply {
                text = "${index + 1}. ${formatTextWithHighlights(item, highlightedWords)}"
                setTextColor(Color.parseColor("#262626"))
                textSize = 14f
                setLineSpacing(0f, 1.6f)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(12))
                }
            }
            listContainer.addView(textView)
        }

        container.addView(listContainer)
    }

    private fun formatTextWithHighlights(text: String, highlightedWords: List<String>): SpannableString {
        val spannable = SpannableString(text)
        highlightedWords.forEach { word ->
            var startIndex = text.indexOf(word)
            while (startIndex >= 0) {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    startIndex + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#171717")),
                    startIndex,
                    startIndex + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                startIndex = text.indexOf(word, startIndex + word.length)
            }
        }
        return spannable
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private fun addBoldText(boldText: String) {
        val textView = TextView(context).apply {
            text = boldText
            setTextColor(Color.parseColor("#262626"))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setLineSpacing(0f, 1.6f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(8), 0, 0)
            }
        }
        container.addView(textView)
    }

    private fun addBulletList(items: List<String>) {
        val listContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dpToPx(20), dpToPx(8), 0, 0)
            }
        }

        items.forEach { item ->
            val textView = TextView(context).apply {
                text = "• $item" // Usa bullet point Unicode
                setTextColor(Color.parseColor("#262626"))
                textSize = 14f
                setLineSpacing(0f, 1.6f)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(8))
                }
            }
            listContainer.addView(textView)
        }

        container.addView(listContainer)
    }

    private fun createTextCell(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            gravity = Gravity.START  // ⬅ Alineación a la izquierda
            layoutParams = TableRow.LayoutParams(
                0, // Usa 0 con peso para que se distribuya proporcionalmente
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
            setTextColor(Color.parseColor("#262626"))
            textSize = 14f
            maxLines = 2 // Puedes ajustar según el diseño
            ellipsize = TextUtils.TruncateAt.END // Opcional, para mostrar "..." si es muy largo
        }
    }

    private fun addTable(headers: List<String>, rows: List<List<String>>) {
        val tableLayout = TableLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(12), 0, dpToPx(12))
            }
            isStretchAllColumns = true // Hace que todas las columnas se expandan
        }

        // Header
        val headerRow = TableRow(context)
        headers.forEach { header ->
            headerRow.addView(createTextCell(header).apply {
                setTypeface(typeface, Typeface.BOLD)
            })
        }
        tableLayout.addView(headerRow)

        // Data Rows
        rows.forEach { row ->
            val tableRow = TableRow(context)
            row.forEach { cell ->
                tableRow.addView(createTextCell(cell)) // ⬅️ Aquí llamas a createTextCell()
            }
            tableLayout.addView(tableRow)
        }

        container?.addView(tableLayout)
    }
}