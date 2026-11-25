package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyRegistrationAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PaymentService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyRegistrationData
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PropertyRegistrationPaymentHandlerTests {
    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    @Mock
    private lateinit var mockPaymentService: PaymentService

    @Mock
    private lateinit var mockPropertyRegistrationDataModelFactory: PropertyRegistrationDataModelFactory

    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockPropertyDetailsTask: PropertyDetailsTask

    @Mock
    private lateinit var mockAddressTask: PropertyRegistrationAddressTask

    private lateinit var handler: PropertyRegistrationPaymentHandler

    private val journeyId = "journey-123"
    private val paymentId = "payment-123"
    private val paymentReference = "payment-reference-123"
    private val uprn = 123L

    @BeforeEach
    fun setUp() {
        handler =
            PropertyRegistrationPaymentHandler(
                mockState,
                mockPaymentService,
                mockPropertyRegistrationDataModelFactory,
                mockAddressAvailabilityService,
            )
    }

    @Test
    fun `createPayment creates a registration payment for the journey using the landlord's email from the start of the journey`() {
        // Arrange
        val quote = PropertyPaymentQuote(amountInPence = 1234, quoteDate = LocalDate.of(2026, 1, 1), renewalDate = LocalDate.of(2027, 1, 1))
        val creationResult = PaymentCreationResult.NewPayment("https://pay.example")
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockState.loggedInLandlordEmailAtStartOfJourney).thenReturn("landlord@example.com")
        whenever(mockPaymentService.createPropertyRegistrationPayment(journeyId, "landlord@example.com", quote)).thenReturn(creationResult)

        // Act
        val result = handler.createPayment(quote)

        // Assert
        assertEquals(creationResult, result)
    }

    @Test
    fun `getStoredPaymentStatus gets the stored status of the journey's payment`() {
        // Arrange
        val storedStatus = PaymentStatusCheckDataModel(paymentId = paymentId, status = PaymentStatus.CREATED)
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockPaymentService.getStoredPropertyRegistrationPaymentStatus(journeyId, paymentReference)).thenReturn(storedStatus)

        // Act
        val result = handler.getStoredPaymentStatus(paymentReference)

        // Assert
        assertEquals(storedStatus, result)
    }

    @Test
    fun `finalisePayment finalises the payment with registration data built from the journey state`() {
        // Arrange
        val registrationData = MockPropertyRegistrationData.createPropertyRegistrationDataModel()
        whenever(mockPropertyRegistrationDataModelFactory.fromJourneyState(mockState)).thenReturn(registrationData)
        whenever(mockPaymentService.finalisePayment(paymentId, PaymentStatus.CAPTURABLE, registrationData))
            .thenReturn(PaymentStatus.SUCCEEDED)

        // Act
        val result = handler.finalisePayment(paymentId, PaymentStatus.CAPTURABLE)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, result)
    }

    @Test
    fun `isPropertyAlreadyRegistered returns false without checking availability when the address has no UPRN`() {
        // Arrange
        stubAddress(AddressDataModel("1 Example Road", uprn = null))

        // Act
        val result = handler.isPropertyAlreadyRegistered()

        // Assert
        assertFalse(result)
        verifyNoInteractions(mockAddressAvailabilityService)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `isPropertyAlreadyRegistered returns whether the address is owned when the address has a UPRN`(isAddressOwned: Boolean) {
        // Arrange
        stubAddress(AddressDataModel("1 Example Road", uprn = uprn))
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(isAddressOwned)

        // Act
        val result = handler.isPropertyAlreadyRegistered()

        // Assert
        assertEquals(isAddressOwned, result)
    }

    private fun stubAddress(address: AddressDataModel) {
        whenever(mockState.propertyDetailsTask).thenReturn(mockPropertyDetailsTask)
        whenever(mockPropertyDetailsTask.addressTask).thenReturn(mockAddressTask)
        whenever(mockAddressTask.getAddress()).thenReturn(address)
    }
}
