package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.exceptions.PropertyAlreadyReactivatedException
import uk.gov.communities.prsdb.webapp.journeys.shared.payment.PaymentHandler
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.CurrentUserService
import uk.gov.communities.prsdb.webapp.services.PaymentService
import uk.gov.communities.prsdb.webapp.services.PropertyReactivationService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PropertyReactivationPaymentHandlerTests {
    @Mock
    private lateinit var mockState: PropertyReactivationJourneyState

    @Mock
    private lateinit var mockPaymentService: PaymentService

    @Mock
    private lateinit var mockPropertyReactivationService: PropertyReactivationService

    @Mock
    private lateinit var mockDataModelFactory: PropertyReactivationDataModelFactory

    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockCurrentUserService: CurrentUserService

    private lateinit var handler: PaymentHandler

    @BeforeEach
    fun setUp() {
        handler =
            PropertyReactivationPaymentHandlerFactory(
                mockPaymentService,
                mockPropertyReactivationService,
                mockDataModelFactory,
                mockAddressAvailabilityService,
                mockCurrentUserService,
            ).create(mockState)
    }

    @Test
    fun `getQuote quotes reactivating the journey's property on the given date`() {
        // Arrange
        val quote = PropertyPaymentQuote(1000, QUOTE_DATE, QUOTE_DATE.plusYears(1))
        whenever(mockState.propertyOwnershipId).thenReturn(PROPERTY_OWNERSHIP_ID)
        whenever(mockPaymentService.getPropertyReactivationPaymentQuote(PROPERTY_OWNERSHIP_ID, QUOTE_DATE)).thenReturn(quote)

        // Act, Assert
        assertEquals(quote, handler.getQuote(QUOTE_DATE))
    }

    @Test
    fun `createPayment creates a reactivation payment for the journey's property, paid by the current user`() {
        // Arrange
        val quote = PropertyPaymentQuote(1000, QUOTE_DATE, QUOTE_DATE.plusYears(1))
        val result = PaymentCreationResult.NewPayment(nextUrl = "https://pay.example/next")
        whenever(mockState.propertyOwnershipId).thenReturn(PROPERTY_OWNERSHIP_ID)
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockCurrentUserService.getCurrentEmail()).thenReturn(EMAIL)
        whenever(mockPaymentService.createPropertyReactivationPayment(PROPERTY_OWNERSHIP_ID, JOURNEY_ID, EMAIL, quote)).thenReturn(result)

        // Act, Assert
        assertEquals(result, handler.createPayment(quote))
    }

    @Test
    fun `getStoredPaymentStatus gets the stored status of the journey property's payment`() {
        // Arrange
        val statusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, PaymentStatus.CREATED)
        whenever(mockState.propertyOwnershipId).thenReturn(PROPERTY_OWNERSHIP_ID)
        whenever(mockPaymentService.getStoredPropertyReactivationPaymentStatus(PROPERTY_OWNERSHIP_ID, PAYMENT_REFERENCE))
            .thenReturn(statusCheck)

        // Act, Assert
        assertEquals(statusCheck, handler.getStoredPaymentStatus(PAYMENT_REFERENCE))
    }

    @Test
    fun `finalisePayment applies the payment by reactivating the paid-for property with the journey's answers`() {
        // Arrange
        val reactivationData = PropertyReactivationDataModel()
        whenever(mockDataModelFactory.fromJourneyState(mockState)).thenReturn(reactivationData)
        whenever(mockPaymentService.finalisePayment(eq(PAYMENT_ID), eq(PaymentStatus.CAPTURABLE), any<(Payment) -> Unit>()))
            .thenReturn(PaymentStatus.SUCCEEDED)

        // Act
        handler.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE)

        // Assert
        val applyPaidChanges = argumentCaptor<(Payment) -> Unit>()
        verify(mockPaymentService).finalisePayment(eq(PAYMENT_ID), eq(PaymentStatus.CAPTURABLE), applyPaidChanges.capture())
        val payment = MockPaymentData.createReactivationPayment(paymentId = PAYMENT_ID)
        applyPaidChanges.firstValue(payment)
        verify(mockPropertyReactivationService).reactivatePropertyForPayment(payment, reactivationData)
    }

    @Test
    fun `finalisePayment cancels any created payment for the property after the payment succeeds`() {
        // Arrange
        whenever(mockState.propertyOwnershipId).thenReturn(PROPERTY_OWNERSHIP_ID)
        whenever(mockPaymentService.finalisePayment(eq(PAYMENT_ID), eq(PaymentStatus.CAPTURABLE), any<(Payment) -> Unit>()))
            .thenReturn(PaymentStatus.SUCCEEDED)

        // Act
        val status = handler.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        inOrder(mockPaymentService, mockPropertyReactivationService) {
            verify(mockPaymentService).finalisePayment(eq(PAYMENT_ID), eq(PaymentStatus.CAPTURABLE), any<(Payment) -> Unit>())
            verify(mockPropertyReactivationService).cancelCreatedPaymentAndLogFailures(PROPERTY_OWNERSHIP_ID)
        }
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["SUCCEEDED"], mode = EnumSource.Mode.EXCLUDE)
    fun `finalisePayment doesn't cancel other payments when the payment didn't succeed`(finalStatus: PaymentStatus) {
        // Arrange
        whenever(mockPaymentService.finalisePayment(eq(PAYMENT_ID), eq(PaymentStatus.CAPTURABLE), any<(Payment) -> Unit>()))
            .thenReturn(finalStatus)

        // Act
        val status = handler.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE)

        // Assert
        assertEquals(finalStatus, status)
        verify(mockPropertyReactivationService, never()).cancelCreatedPaymentAndLogFailures(any())
    }

    @Test
    fun `finalisePayment doesn't cancel other payments when finalising throws`() {
        // Arrange
        whenever(mockPaymentService.finalisePayment(eq(PAYMENT_ID), eq(PaymentStatus.CAPTURABLE), any<(Payment) -> Unit>()))
            .thenThrow(PropertyAlreadyReactivatedException(PROPERTY_OWNERSHIP_ID))

        // Act & Assert
        assertThrows<PropertyAlreadyReactivatedException> { handler.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE) }

        verify(mockPropertyReactivationService, never()).cancelCreatedPaymentAndLogFailures(any())
    }

    @Test
    fun `isPropertyAlreadyRegistered is false without checking when the property has no UPRN`() {
        // Arrange
        whenever(mockState.uprn).thenReturn(null)

        // Act, Assert
        assertFalse(handler.isPropertyAlreadyRegistered())
        verifyNoInteractions(mockAddressAvailabilityService)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `isPropertyAlreadyRegistered is whether another property owns the property's address`(isOwnedByAnotherProperty: Boolean) {
        // Arrange
        whenever(mockState.propertyOwnershipId).thenReturn(PROPERTY_OWNERSHIP_ID)
        whenever(mockState.uprn).thenReturn(UPRN)
        whenever(mockAddressAvailabilityService.hasAnotherActiveRegistration(UPRN, PROPERTY_OWNERSHIP_ID))
            .thenReturn(isOwnedByAnotherProperty)

        // Act, Assert
        assertEquals(isOwnedByAnotherProperty, handler.isPropertyAlreadyRegistered())
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 12L
        private const val UPRN = 34L
        private const val JOURNEY_ID = "journey-id"
        private const val EMAIL = "landlord@example.com"
        private const val PAYMENT_ID = "payment-id"
        private const val PAYMENT_REFERENCE = "payment-reference"
        private val QUOTE_DATE = LocalDate.of(2026, 5, 1)
    }
}
