package dev.ktown.cardspendtracker.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddTransactionScreenLogicTest {

    @Test
    fun `filterDollarAmountInput accepts valid amount patterns`() {
        assertEquals("", filterDollarAmountInput(""))
        assertEquals("12", filterDollarAmountInput("12"))
        assertEquals("12.3", filterDollarAmountInput("12.3"))
        assertEquals("0.99", filterDollarAmountInput("0.99"))
    }

    @Test
    fun `filterDollarAmountInput rejects invalid amount patterns`() {
        assertEquals(null, filterDollarAmountInput("12..3"))
        assertEquals(null, filterDollarAmountInput("12.345"))
        assertEquals(null, filterDollarAmountInput("abc"))
        assertEquals(null, filterDollarAmountInput("12a"))
    }

    @Test
    fun `isValidDollarAmount requires positive numeric value`() {
        assertTrue(isValidDollarAmount("12.34"))
        assertFalse(isValidDollarAmount(""))
        assertFalse(isValidDollarAmount("0"))
        assertFalse(isValidDollarAmount("-1"))
        assertFalse(isValidDollarAmount("abc"))
    }
}
