package com.example.kiblasalat

import com.batoulapps.adhan.CalculationMethod
import org.junit.Test

class AdhanTest {
    @Test
    fun printMethods() {
        CalculationMethod.values().forEach {
            println("METHOD: ${it.name}")
        }
    }
}
