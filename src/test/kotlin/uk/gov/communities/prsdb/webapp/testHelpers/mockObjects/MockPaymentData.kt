package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperty
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.DEFAULT_PAYMENT_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPrsdbUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData.Companion.createSavedJourneyState
import java.time.Instant
import java.time.LocalDate

class MockPaymentData {
    companion object {
        fun createPayment(
            paymentId: String = DEFAULT_PAYMENT_ID,
            amountInPence: Int = 1000,
            reference: String = "reference",
            paymentCreatedAt: Instant = Instant.now(),
            forPeriodEnding: LocalDate = LocalDate.of(2027, 3, 1),
            status: PaymentStatus = PaymentStatus.CREATED,
            incompleteProperty: LandlordIncompleteProperty = LandlordIncompleteProperty(createPrsdbUser(), createSavedJourneyState()),
        ) = Payment(
            paymentId = paymentId,
            amountInPence = amountInPence,
            reference = reference,
            paymentCreatedAt = paymentCreatedAt,
            forPeriodEnding = forPeriodEnding,
            status = status,
            incompleteProperty = incompleteProperty,
        )
    }
}
