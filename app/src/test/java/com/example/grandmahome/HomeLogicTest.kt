package com.example.grandmahome

import org.junit.Assert.*
import org.junit.Test

class HomeLogicTest {
    @Test
    fun onlyFiveConsecutiveTapsOpenSettings() {
        val gate = ClockTapGate()
        (1..4).forEach { assertFalse(gate.tap(it * 200L)) }
        assertTrue(gate.tap(1000))
        assertFalse(gate.tap(1200))
    }

    @Test
    fun slowTapsResetTheSequence() {
        val gate = ClockTapGate()
        (1..4).forEach { assertFalse(gate.tap(it * 200L)) }
        assertFalse(gate.tap(3000))
        (1..3).forEach { assertFalse(gate.tap(3000 + it * 200L)) }
        assertTrue(gate.tap(3800))
    }
}
