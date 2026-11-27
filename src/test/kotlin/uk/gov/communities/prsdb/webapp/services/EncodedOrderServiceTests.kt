package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.clients.EncodedClient
import uk.gov.communities.prsdb.webapp.exceptions.EncodedException
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedCreateOrderRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedGovUkPayAttributes
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedOrder
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedOrderAttributes
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedOrderLinks
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedPayNowOptions
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedPayNowOrder
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class EncodedOrderServiceTests {
    @Mock
    private lateinit var encodedClient: EncodedClient

    @InjectMocks
    private lateinit var encodedOrderService: EncodedOrderService

    @Test
    fun `createPayNowOrder creates a GBP order with the GOV UK Pay payment details and return URL`() {
        // Arrange
        whenever(encodedClient.createOrder(any())).thenReturn(createdOrder())

        // Act
        createPayNowOrder(amountInPence = 6500)

        // Assert
        verify(encodedClient).createOrder(
            EncodedCreateOrderRequest(
                ref = REFERENCE,
                description = DESCRIPTION,
                currency = "GBP",
                totalAmount = BigDecimal("65.00"),
                attributes =
                    EncodedOrderAttributes(
                        govUkPay = EncodedGovUkPayAttributes(paymentId = GOV_UK_PAY_PAYMENT_ID, oneTimeToken = ONE_TIME_TOKEN),
                    ),
                paynow = EncodedPayNowOptions(returnUrl = RETURN_URL),
            ),
        )
    }

    // BigDecimal equality includes scale, so these also check the amount keeps two decimal places
    @ParameterizedTest
    @CsvSource("1, 0.01", "6500, 65.00", "6550, 65.50")
    fun `createPayNowOrder converts the amount from pence to pounds`(
        amountInPence: Int,
        expectedTotalAmount: String,
    ) {
        // Arrange
        whenever(encodedClient.createOrder(any())).thenReturn(createdOrder())

        // Act
        createPayNowOrder(amountInPence = amountInPence)

        // Assert
        val requestCaptor = argumentCaptor<EncodedCreateOrderRequest>()
        verify(encodedClient).createOrder(requestCaptor.capture())
        assertEquals(BigDecimal(expectedTotalAmount), requestCaptor.firstValue.totalAmount)
    }

    @Test
    fun `createPayNowOrder returns the Encoded order ID and PayNow URL`() {
        // Arrange
        whenever(encodedClient.createOrder(any())).thenReturn(createdOrder())

        // Act
        val payNowOrder = createPayNowOrder()

        // Assert
        assertEquals(EncodedPayNowOrder(orderId = ORDER_ID, payNowUrl = PAYNOW_URL), payNowOrder)
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = [" "])
    fun `createPayNowOrder throws EncodedException when the order has no PayNow link`(payNowUrl: String?) {
        // Arrange
        whenever(encodedClient.createOrder(any())).thenReturn(createdOrder(payNowUrl))

        // Act
        val exception = assertThrows(EncodedException::class.java) { createPayNowOrder() }

        // Assert
        assertEquals("Encoded order $ORDER_ID had no PayNow link", exception.message)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1])
    fun `createPayNowOrder rejects non-positive amounts without creating an order`(amountInPence: Int) {
        // Act & Assert
        assertThrows(IllegalArgumentException::class.java) { createPayNowOrder(amountInPence = amountInPence) }
        verifyNoInteractions(encodedClient)
    }

    private fun createPayNowOrder(amountInPence: Int = 6500) =
        encodedOrderService.createPayNowOrder(
            amountInPence = amountInPence,
            reference = REFERENCE,
            description = DESCRIPTION,
            govUkPayPaymentId = GOV_UK_PAY_PAYMENT_ID,
            oneTimeToken = ONE_TIME_TOKEN,
            returnUrl = RETURN_URL,
        )

    private fun createdOrder(payNowUrl: String? = PAYNOW_URL) =
        EncodedOrder(id = ORDER_ID, ref = REFERENCE, links = EncodedOrderLinks(paynow = payNowUrl))

    companion object {
        private const val REFERENCE = "f0a3c9d2-6b1e-4c3a-9d6f-2a7b8c9d0e1f"
        private const val DESCRIPTION = "Property registration fee"
        private const val GOV_UK_PAY_PAYMENT_ID = "hu20sqlact5260q2nanm0q8u93"
        private const val ONE_TIME_TOKEN = "gov-uk-pay-one-time-token"
        private const val RETURN_URL = "https://example.com/encoded/return"
        private const val ORDER_ID = "96d7d98a-46fd-4d3b-a15b-b03c116ae949"
        private const val PAYNOW_URL = "https://sit.encoded.services/paynow/$ORDER_ID"
    }
}
