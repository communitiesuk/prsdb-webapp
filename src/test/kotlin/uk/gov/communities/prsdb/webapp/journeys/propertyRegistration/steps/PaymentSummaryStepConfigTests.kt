package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyRegistrationAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PaymentService
import kotlin.test.assertIs

@ExtendWith(MockitoExtension::class)
class PaymentSummaryStepConfigTests {
    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockPaymentService: PaymentService

    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    @Mock
    private lateinit var mockPropertyDetailsTask: PropertyDetailsTask

    @Mock
    private lateinit var mockAddressTask: PropertyRegistrationAddressTask

    private lateinit var stepConfig: PaymentSummaryStepConfig

    private val journeyId = "journey-123"
    private val uprn = 123L
    private val defaultDestination = Destination.ExternalUrl("default")

    @BeforeEach
    fun setUp() {
        stepConfig = PaymentSummaryStepConfig(mockAddressAvailabilityService, mockPaymentService)
        whenever(mockState.propertyDetailsTask).thenReturn(mockPropertyDetailsTask)
        whenever(mockPropertyDetailsTask.addressTask).thenReturn(mockAddressTask)
    }

    @Test
    fun `afterStepDataIsAdded marks the address as already registered when it has been registered since the journey started`() {
        // Arrange
        whenever(mockAddressTask.getAddress()).thenReturn(AddressDataModel("1 Example Road, EG1 2AB", uprn = uprn))
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(true)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockAddressTask).isAddressAlreadyRegistered = true
    }

    @Test
    fun `afterStepDataIsAdded does not mark the address as already registered when it is still available`() {
        // Arrange
        whenever(mockAddressTask.getAddress()).thenReturn(AddressDataModel("1 Example Road, EG1 2AB", uprn = uprn))
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockAddressTask, never()).isAddressAlreadyRegistered = anyOrNull()
    }

    @Test
    fun `afterStepDataIsAdded does not check registration when the address has no UPRN`() {
        // Arrange
        whenever(mockAddressTask.getAddress()).thenReturn(AddressDataModel("1 Example Road, EG1 2AB", uprn = null))

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verifyNoInteractions(mockAddressAvailabilityService)
        verify(mockAddressTask, never()).isAddressAlreadyRegistered = anyOrNull()
    }

    @Test
    fun `resolveNextDestination redirects to the already registered step when the address is already registered`() {
        // Arrange
        whenever(mockAddressTask.isAddressAlreadyRegistered).thenReturn(true)
        val mockAlreadyRegisteredStep = mock<AlreadyRegisteredStep>()
        whenever(mockAlreadyRegisteredStep.currentJourneyId).thenReturn(journeyId)
        whenever(mockAddressTask.alreadyRegisteredStep).thenReturn(mockAlreadyRegisteredStep)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(mockAlreadyRegisteredStep, assertIs<Destination.VisitableStep>(result).step)
        verifyNoInteractions(mockPaymentService)
    }

    @Test
    fun `resolveNextDestination creates a payment and redirects to GOV UK Pay when the address is not already registered`() {
        // Arrange
        whenever(mockAddressTask.isAddressAlreadyRegistered).thenReturn(false)
        stubPaymentCreation()

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals("https://pay.example.test/next", assertIs<Destination.ExternalUrl>(result).externalUrl)
    }

    @Test
    fun `resolveNextDestination clears the previous payment outcome before creating a new payment`() {
        // Arrange
        whenever(mockAddressTask.isAddressAlreadyRegistered).thenReturn(false)
        stubPaymentCreation()

        // Act
        stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        val inOrder = inOrder(mockState, mockPaymentService)
        inOrder.verify(mockState).paymentOutcome = null
        inOrder.verify(mockPaymentService).createPropertyRegistrationPayment(any(), any())
    }

    private fun stubPaymentCreation() {
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockState.loggedInLandlordEmailAtStartOfJourney).thenReturn("landlord@example.com")
        whenever(mockPaymentService.createPropertyRegistrationPayment(journeyId, "landlord@example.com"))
            .thenReturn("https://pay.example.test/next")
    }
}
