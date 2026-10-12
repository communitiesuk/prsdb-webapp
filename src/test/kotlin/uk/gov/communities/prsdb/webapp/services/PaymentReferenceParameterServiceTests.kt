package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.ServletRequest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PaymentReferenceParameterServiceTests {
    @Test
    fun `getParameterOrNull returns the paymentReference parameter value when present`() {
        // Arrange
        val request = mock<ServletRequest>()
        whenever(request.getParameter("paymentReference")).thenReturn("payment-reference")
        val service = PaymentReferenceParameterService(request)

        // Act
        val paymentReference = service.getParameterOrNull()

        // Assert
        assertEquals("payment-reference", paymentReference)
    }

    @Test
    fun `getParameterOrNull returns null when the paymentReference parameter is absent`() {
        // Arrange
        val request = mock<ServletRequest>()
        whenever(request.getParameter("paymentReference")).thenReturn(null)
        val service = PaymentReferenceParameterService(request)

        // Act
        val paymentReference = service.getParameterOrNull()

        // Assert
        assertNull(paymentReference)
    }
}
