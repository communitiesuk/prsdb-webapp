package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentHelperTests {
    @Test
    fun `formatPence omits the pence for a whole number of pounds`() {
        assertEquals("£65", PaymentHelper.formatPence(6500))
    }

    @Test
    fun `formatPence shows two decimal places when there are pence`() {
        assertEquals("£12.34", PaymentHelper.formatPence(1234))
    }

    @Test
    fun `formatPence pads a single digit of pence to two decimal places`() {
        assertEquals("£12.05", PaymentHelper.formatPence(1205))
    }

    @Test
    fun `formatPence formats amounts under a pound`() {
        assertEquals("£0.50", PaymentHelper.formatPence(50))
    }

    @Test
    fun `formatPence formats zero`() {
        assertEquals("£0", PaymentHelper.formatPence(0))
    }
}
