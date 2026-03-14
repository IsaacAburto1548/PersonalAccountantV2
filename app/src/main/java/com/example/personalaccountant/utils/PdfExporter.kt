package com.example.personalaccountant.utils

import android.content.Context
import android.os.Environment
import com.example.personalaccountant.data.Transaction
import com.example.personalaccountant.data.Account
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfExporter(private val context: Context) {

    fun generateMonthlyReport(
        transactions: List<Transaction>,
        accounts: List<Account>,
        monthName: String,
        year: Int
    ): File? {
        try {
            val fileName = "Reporte_Financify_${monthName}_${year}.pdf"
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )

            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Header
            document.add(
                Paragraph("Reporte Mensual de Gastos")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24f)
                    .setBold()
                    .setFontColor(DeviceRgb(46, 125, 50)) // IncomeGreen
            )

            document.add(
                Paragraph("Periodo: $monthName $year")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14f)
            )

            document.add(Paragraph("\n"))

            // Accounts Summary
            document.add(Paragraph("Resumen de Cuentas").setBold().setFontSize(16f))
            val accountTable = Table(UnitValue.createPointArray(floatArrayOf(300f, 200f)))
            accountTable.addHeaderCell(Cell().add(Paragraph("Cuenta").setBold()))
            accountTable.addHeaderCell(Cell().add(Paragraph("Saldo").setBold()))

            accounts.forEach { account ->
                accountTable.addCell(account.name)
                accountTable.addCell(formatCurrencyWithSymbol(account.currentBalance))
            }
            document.add(accountTable)

            document.add(Paragraph("\n"))

            // Transactions Table
            document.add(Paragraph("Detalle de Movimientos").setBold().setFontSize(16f))
            val transTable = Table(UnitValue.createPointArray(floatArrayOf(100f, 200f, 100f, 100f)))
            transTable.addHeaderCell(Cell().add(Paragraph("Fecha").setBold()))
            transTable.addHeaderCell(Cell().add(Paragraph("Descripción").setBold()))
            transTable.addHeaderCell(Cell().add(Paragraph("Categoría").setBold()))
            transTable.addHeaderCell(Cell().add(Paragraph("Monto").setBold()))

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            transactions.forEach { tx ->
                transTable.addCell(sdf.format(Date(tx.date)))
                transTable.addCell(tx.description)
                transTable.addCell(tx.category)
                val amountText = if (tx.type == "EXPENSE") "-${formatCurrency(tx.amount)}" else formatCurrency(tx.amount)
                transTable.addCell(amountText)
            }
            document.add(transTable)

            // Totals
            val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            
            document.add(Paragraph("\n"))
            document.add(Paragraph("Total Ingresos: ${formatCurrencyWithSymbol(totalIncome)}").setFontColor(DeviceRgb(46, 125, 50)))
            document.add(Paragraph("Total Gastos: ${formatCurrencyWithSymbol(totalExpense)}").setFontColor(DeviceRgb(211, 47, 47)))
            document.add(Paragraph("Balance Neto: ${formatCurrencyWithSymbol(totalIncome - totalExpense)}").setBold())

            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
