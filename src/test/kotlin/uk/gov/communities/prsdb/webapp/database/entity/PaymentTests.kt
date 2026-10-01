package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPayment
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentState
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPrsdbUserData.Companion.createPrsdbUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import java.time.Instant
import java.time.LocalDate

class PaymentTests {
    @Test
    fun `associateWithProperty sets the associated property and clears the associated incomplete property`() {
        // Arrange
        val user = createPrsdbUser()
        val incompleteProperty = LandlordIncompleteProperty(user, MockSavedJourneyStateData.createSavedJourneyState())
        val payment =
            Payment(
                paymentId = "payment-id",
                amountInPence = 100,
                reference = "reference",
                paymentCreatedAt = Instant.now(),
                forPeriodEnding = LocalDate.now(),
                status = PaymentStatus.CREATED,
                incompleteProperty = incompleteProperty,
            )
        val property = MockLandlordData.createPropertyOwnership()

        // Act
        payment.associateWithProperty(property)

        // Assert
        assertEquals(property, payment.associatedProperty)
        assertNull(payment.associatedIncompleteProperty)
    }

    @Test
    fun `fromGovUkPay creates a Payment with the fields mapped from the GovUkPayPayment`() {
        // Arrange
        val user = createPrsdbUser()
        val incompleteProperty = LandlordIncompleteProperty(user, MockSavedJourneyStateData.createSavedJourneyState())
        val createdDate = Instant.now()
        val periodEnding = LocalDate.now()
        val govUkPayPayment =
            GovUkPayPayment(
                paymentId = "payment-id",
                amount = 100,
                reference = "reference",
                description = "description",
                createdDate = createdDate,
                state = GovUkPayPaymentState(status = GovUkPayPaymentStatus.SUCCESS, finished = true),
            )

        // Act
        val payment = Payment.fromGovUkPay(govUkPayPayment, periodEnding, incompleteProperty)

        // Assert
        assertEquals("payment-id", payment.paymentId)
        assertEquals(100, payment.amountInPence)
        assertEquals("reference", payment.reference)
        assertEquals(createdDate, payment.paymentCreatedAt)
        assertEquals(periodEnding, payment.forPeriodEnding)
        assertEquals(PaymentStatus.SUCCEEDED, payment.status)
        assertEquals(incompleteProperty, payment.associatedIncompleteProperty)
        assertEquals(user, payment.payingUser)
    }
}
