package com.example.personalaccountant.utils

import android.content.Context
import android.os.Environment
import com.example.personalaccountant.data.Account
import com.example.personalaccountant.data.Transaction
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

class CsvExporter(private val context: Context) {

    fun generateMonthlyReport(
        transactions: List<Transaction>,
        accounts: List<Account>,
        monthName: String,
        year: Int
    ): File? {
        try {
            val fileName = "Reporte_Financify_${monthName}_${year}.csv"
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )

            // Ensure CSV has UTF-8 BOM so Excel opens it correctly with accents
            PrintWriter(FileWriter(file)).use { writer ->
                // Write BOM
                writer.print('\ufeff')
                
                writer.println("REPORTE FINANCIERO - FINANCIFY")
                writer.println("Periodo: $monthName $year")
                writer.println("")
                
                writer.println("RESUMEN DE CUENTAS")
                writer.println("Cuenta,Saldo")
                accounts.forEach { account ->
                    val safeName = account.name.replace(",", " ")
                    writer.println("$safeName,${account.currentBalance}")
                }
                
                writer.println("")
                writer.println("DETALLE DE MOVIMIENTOS")
                writer.println("Fecha y Hora,Tipo,Categoria,Descripcion,Monto")
                
                transactions.forEach { tx ->
                    val txType = if(tx.type == "EXPENSE") "Gasto" else if(tx.type == "INCOME") "Ingreso" else "Pago Tarjeta"
                    val amount = if (tx.type == "EXPENSE") "-${tx.amount}" else "${tx.amount}"
                    val dateStr = formatDateTime(tx.date).replace(",", " ")
                    val desc = tx.description.replace(",", " ")
                    val cat = tx.category.replace(",", " ")
                    
                    writer.println("$dateStr,$txType,$cat,$desc,$amount")
                }
                
                writer.println("")
                val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                writer.println("RESUMEN TOTAL")
                writer.println("Total Ingresos,$totalIncome")
                writer.println("Total Gastos,$totalExpense")
                writer.println("Balance Neto,${totalIncome - totalExpense}")
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
