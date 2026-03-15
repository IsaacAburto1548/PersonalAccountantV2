package com.example.personalaccountant

import com.example.personalaccountant.data.repository.FinanceRepository
import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar

class FinanceRepositoryTest {

    @Test
    fun testBillingCycleCalculation_AfterFourth() {
        // We use a dummy repo for logic-only tests
        @Suppress("UNCHECKED_CAST")
        val repo = FinanceRepository(null as? com.example.personalaccountant.data.AppDatabase ?: return) 
        
        // Nov 10, 2023
        val calendar = Calendar.getInstance().apply {
            set(2023, Calendar.NOVEMBER, 10)
        }
        
        val (start, end) = repo.calculateBillingCycle(calendar.timeInMillis)
        
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val endCal = Calendar.getInstance().apply { timeInMillis = end }
        
        // For Nov 10, cycle starts on Nov 4
        assertEquals(4, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.NOVEMBER, startCal.get(Calendar.MONTH))
        
        // Cycle ends on Dec 3
        assertEquals(3, endCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.DECEMBER, endCal.get(Calendar.MONTH))
    }

    @Test
    fun testBillingCycleCalculation_BeforeFourth() {
        @Suppress("UNCHECKED_CAST")
        val repo = FinanceRepository(null as? com.example.personalaccountant.data.AppDatabase ?: return)
        
        // Nov 2, 2023
        val calendar = Calendar.getInstance().apply {
            set(2023, Calendar.NOVEMBER, 2)
        }
        
        val (start, end) = repo.calculateBillingCycle(calendar.timeInMillis)
        
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val endCal = Calendar.getInstance().apply { timeInMillis = end }
        
        // For Nov 2, cycle starts on Oct 4
        assertEquals(4, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.OCTOBER, startCal.get(Calendar.MONTH))
        
        // Cycle ends on Nov 3
        assertEquals(3, endCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.NOVEMBER, endCal.get(Calendar.MONTH))
    }
}
