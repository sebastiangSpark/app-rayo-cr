package com.rayo.rayoxml.utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

import android.graphics.Canvas
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PDFGenerator {

    fun createAndDownloadPdf(context: Context, contactoPyZ: ContactoPyZ, prestamoPyZ: PrestamoPyZ, fileName: String = "PS_${prestamoPyZ.codigo}.pdf") {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        //val paint = Paint()
        // Create Paint for normal text

        val leftMargin = 60f
        val rightMargin = 50f

        val paint = Paint()
        paint.textSize = 16f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // Create Paint for bold text
        val boldPaint = Paint()
        boldPaint.textSize = 16f
        boldPaint.color = Color.BLACK
        boldPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        /*// Draw normal text
        canvas.drawText("Hello, this is ", 100f, 200f, paint)

        // Draw bold text
        canvas.drawText("BOLD TEXT", 200f, 200f, boldPaint)*/

        // Get formatted date
        val months = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val date = Calendar.getInstance()
        val month = months[date.get(Calendar.MONTH)]
        val currentDate = "${date.get(Calendar.DAY_OF_MONTH)} de $month del ${date.get(Calendar.YEAR)}"

        // Draw logo
        val logo = getBitmapFromAssets(context, "img/logo_rayo.png")
        logo?.let { canvas.drawBitmap(Bitmap.createScaledBitmap(it, 160, 65, true), leftMargin, 50f, paint) }

        // Draw background image
        val background = getBitmapFromAssets(context, "img/rayo_logo_fondo.jpeg")
        background?.let { canvas.drawBitmap(Bitmap.createScaledBitmap(it, 469, 262, true), leftMargin, 320f, paint) }

        // Draw QR code (Needs implementation)
        /*val qrBitmap = generateQRCode(prestamoPyZ.idPrestamo)
        qrBitmap?.let { canvas.drawBitmap(Bitmap.createScaledBitmap(it, 35, 35, true), 158f, 25f, paint) }*/
        val qrBitmap = generateQRCode(prestamoPyZ.idPrestamo)
        qrBitmap?.let {
            val scaledQR = Bitmap.createScaledBitmap(it, 100, 100, true) // Adjust size
            canvas.drawBitmap(scaledQR, 450f, leftMargin, paint)
        }

        // Text: Bogotá date
        paint.color = Color.BLACK
        paint.textSize = 12f
        canvas.drawText("Bogotá, $currentDate", 370f, 180f, paint)

        // Title
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("PAZ Y SALVO", 200f, 285f, paint)

        // Text content
        paint.textSize = 14f
        paint.isFakeBoldText = false

        val maxTextWidth = canvas.width - leftMargin - rightMargin
        val textX = leftMargin
        var textY = 320f
        val lineSpacing = 20f  // Ajusta el espaciado entre líneas

        // Construcción del texto dinámico
        val nombre = contactoPyZ.nombre.uppercase()
        val apellidos = contactoPyZ.apellidos.uppercase()
        val documento = contactoPyZ.numeroDocumento
        val codigoPrestamo = prestamoPyZ.codigo
        val fechaDesembolso = prestamoPyZ.fechaDesembolso
        val nombreEmpresa = "RAYO SOLUCIONES FINANCIERAS"
        val nit = "901287068-0"
        //val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val text = """
            $nombreEmpresa con Nit $nit, certifica que el Señor(a) $nombre $apellidos, identificado con cédula de ciudadanía N° $documento, se encuentra a paz y salvo de todo concepto con respecto a la obligación N° $codigoPrestamo con fecha de desembolso el $fechaDesembolso.
            
            
            El presente se expide el día $currentDate.
            
            
            Atentamente,
        """.trimIndent()

        /*text.lineSequence().forEach {
            canvas.drawText(it, textX, textY, paint)
            textY += 15f
        }*/
        // Lista de palabras/frases dinámicas a resaltar en negrita
        val boldWords = listOf(nombreEmpresa, nit, nombre, apellidos, documento, codigoPrestamo, fechaDesembolso)

        text.split("\n").forEach { paragraph ->
            if (paragraph.isBlank()) {
                textY += lineSpacing  // Espacio para líneas en blanco
            } else {
                val words = paragraph.split(" ")
                var line = ""

                for (word in words) {
                    val testLine = if (line.isEmpty()) word else "$line $word"

                    // Verificar si la línea con la nueva palabra excede el ancho máximo
                    if (paint.measureText(testLine) > maxTextWidth) {
                        // Dibujar la línea antes de que se corte
                        drawStyledText(canvas, line, textX, textY, paint, boldWords)
                        textY += lineSpacing
                        line = word  // Nueva línea con la palabra actual
                    } else {
                        line = testLine
                    }
                }

                // Dibujar la última línea si hay palabras restantes
                if (line.isNotEmpty()) {
                    drawStyledText(canvas, line, textX, textY, paint, boldWords)
                    textY += lineSpacing
                }
            }
        }

        // Draw signature
        val signature = getBitmapFromAssets(context, "img/firma_juancarlos.png")
        signature?.let { canvas.drawBitmap(Bitmap.createScaledBitmap(it, 200, 123, true), leftMargin, 550f, paint) }

        // Signature text
        canvas.drawText("____________________________", leftMargin, 660f, boldPaint)
        canvas.drawText("Juancarlos Bermudez", leftMargin, 680f, boldPaint)
        canvas.drawText("Supervisor de Cobros", leftMargin, 700f, boldPaint)

        pdfDocument.finishPage(page)

        // Guardar PDF
        savePdfPrivately(context, pdfDocument, fileName)
        //savePdfToExternalStorage(context, pdfDocument, fileName)
        //checkPermissionAndSavePdf(context, pdfDocument, "paz_y_salvo.pdf")
    }

    // Función para dibujar texto con palabras en negrita
    fun drawStyledText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, boldWords: List<String>) {
        var currentX = x
        val originalTypeface = paint.typeface

        // Separar palabras y mantener espacios intactos
        val wordsWithSpaces = text.split(Regex("(?<=\\s)|(?=\\s)"))

        wordsWithSpaces.forEach { part ->
            val cleanWord = part.trim().trimEnd(',', '.', ';', ':', '?', '!') // Elimina signos de puntuación al final

            // Verifica si la palabra sin signos debe ir en negrita
            val isBold = boldWords.any { it.equals(cleanWord, ignoreCase = true) }

            // Aplicar negrita si corresponde
            paint.typeface = if (isBold) Typeface.DEFAULT_BOLD else originalTypeface

            // Dibujar el texto
            canvas.drawText(part, currentX, y, paint)
            currentX += paint.measureText(part) // Mover X para la siguiente palabra
        }

        // Restaurar el tipo de letra original
        paint.typeface = originalTypeface
    }

    private fun getBitmapFromAssets(context: Context, fileName: String): Bitmap? {
        return try {
            context.assets.open(fileName).use { BitmapFactory.decodeStream(it) }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun generateQRCode(data: String): Bitmap? {
        val qrUrl = "https://quickchart.io/qr?size=100&format=png&text=${data}"

        return try {
            val barcodeEncoder = BarcodeEncoder()
            val bitMatrix: BitMatrix = com.google.zxing.qrcode.QRCodeWriter()
                .encode(qrUrl, BarcodeFormat.QR_CODE, 100, 100)

            barcodeEncoder.createBitmap(bitMatrix) // Returns the Bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    fun savePdfPrivately(context: Context, pdfDocument: PdfDocument, fileName: String) {
        val pdfDir = context.getExternalFilesDir(null) // Carpeta privada para la app
        val pdfFile = File(pdfDir, fileName)

        try {
            FileOutputStream(pdfFile).use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            println("✅ PDF guardado en carpeta privada: ${pdfFile.absolutePath}")

            /*Toast.makeText(
                context,
                "Archivo guardado en: ${pdfFile.absolutePath}",
                Toast.LENGTH_LONG
            ).show()*/

            openPdf(context, pdfFile)

        } catch (e: IOException) {
            e.printStackTrace()
            /*Toast.makeText(
                context,
                "Error al guardar PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()*/
        }
    }


    fun savePdfToExternalStorage(context: Context, pdfDocument: PdfDocument, fileName: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val pdfFile = File(downloadsDir, fileName)

        try {
            FileOutputStream(pdfFile).use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            println("PDF saved at: ${pdfFile.absolutePath}")

            /*Toast.makeText(
                context,
                "Paz y Salvo guardado en: ${pdfFile.absolutePath}",
                Toast.LENGTH_SHORT
            ).show()*/

            // Open PDF after saving
            openPdf(context, pdfFile)

        } catch (e: IOException) {
            e.printStackTrace()
            /*Toast.makeText(
                context,
                "Error al guardar Paz y Salvo: ${e.message}",
                Toast.LENGTH_LONG
            ).show()*/
        }
    }

    // Function to open PDF using default viewer
    fun openPdf(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Allow access to other apps
            }

            context.startActivity(Intent.createChooser(intent, "Abrir PDF con"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No se encontró una aplicación para abrir el PDF", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkPermissionAndSavePdf(context: Context, pdfDocument: PdfDocument, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ no necesita permiso para guardar en Downloads
            savePdfToExternalStorage(context, pdfDocument, fileName)
        } else {
            // Android 9 o inferior: verificar y pedir permisos
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                val activity = context as? Activity
                activity?.let {
                    // Ya puedes usar it para ActivityCompat, etc.
                    ActivityCompat.requestPermissions(
                        it,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        200
                    )
                }
            } else {
                // Permiso ya concedido
                savePdfToExternalStorage(context, pdfDocument, fileName)
            }
        }
    }

}

// Data classes for Contacto & Prestamo
data class ContactoPyZ(val nombre: String, val apellidos: String, val numeroDocumento: String)
data class PrestamoPyZ(val idPrestamo: String, val codigo: String, val fechaDesembolso: String)