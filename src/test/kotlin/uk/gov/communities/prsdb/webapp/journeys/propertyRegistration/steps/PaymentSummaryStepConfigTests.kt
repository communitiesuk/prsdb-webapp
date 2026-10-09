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
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyRegistrationAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentQuote
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PaymentService
import java.net.URI
import java.time.LocalDate
import kotlin.test.assertIs

@ExtendWith(MockitoExtension::class)
class PaymentSummaryStepConfigTests {
    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockPaymentService: PaymentService

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    @Mock
    private lateinit var mockPropertyDetailsTask: PropertyDetailsTask

    @Mock
    private lateinit var mockAddressTask: PropertyRegistrationAddressTask

    @Mock
    private lateinit var mockPaymentSummaryStep: PaymentSummaryStep

    private lateinit var stepConfig: PaymentSummaryStepConfig

    private val journeyId = "journey-123"
    private val uprn = 123L
    private val defaultDestination = Destination.ExternalUrl("default")
    private val today = LocalDate.now(DateTimeHelper.UK_ZONE)

    @BeforeEach
    fun setUp() {
        stepConfig =
            PaymentSummaryStepConfig(
                mockAddressAvailabilityService,
                mockPaymentService,
                mockAbsoluteUrlProvider,
            )
    }

    @Test
    fun `getStepSpecificContent stores a newly calculated quote in journey state`() {
        // Arrange
        val quote = createQuote(today)
        whenever(mockPaymentService.getPropertyRegistrationPaymentQuote()).thenReturn(quote)

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("Payment summary (TODO PDJB-996)", content["todoComment"])
        verify(mockState).paymentQuote = quote
    }

    @Test
    fun `afterStepDataIsAdded marks the address as already registered when it has been registered since the journey started`() {
        // Arrange
        stubAddressTask()
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
        stubAddressTask()
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
        stubAddressTask()
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
        stubAddressTask()
        whenever(mockAddressTask.isAddressAlreadyRegistered).thenReturn(true)
        val mockAlreadyRegisteredStep = mock<AlreadyRegisteredStep>()
        whenever(mockAlreadyRegisteredStep.currentJourneyId).thenReturn(journeyId)
        whenever(mockAddressTask.alreadyRegisteredStep).thenReturn(mockAlreadyRegisteredStep)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(mockAlreadyRegisteredStep, assertIs<Destination.VisitableStep>(result).step)
        verifyNoInteractions(mockPaymentService)
        verify(mockState, never()).paymentQuote
    }

    @Test
    fun `resolveNextDestination creates a payment and redirects to GOV UK Pay when the address is not already registered`() {
        // Arrange
        stubAddressTask()
        whenever(mockAddressTask.isAddressAlreadyRegistered).thenReturn(false)
        stubPaymentCreation()

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals("https://pay.example.test/next", assertIs<Destination.ExternalUrl>(result).externalUrl)
        verify(mockPaymentService).createPropertyRegistrationPayment(
            journeyId,
            "https://example.test/landlord/register-property/payment-return?journeyId=$journeyId",
            "landlord@example.com",
            createQuote(today),
        )
        verify(mockPaymentService, never()).getPropertyRegistrationPaymentQuote(any())
        verify(mockState, never()).paymentQuote = anyOrNull()
    }

    @Test
    fun `resolveNextDestination refreshes a missing quote without creating a payment`() {
        // Arrange
        stubAddressTask()
        stubSummaryDestination()
        whenever(mockAddressTask.isAddressAlreadyRegistered).thenReturn(false)
        val refreshedQuote = createQuote(today)
        whenever(mockPaymentService.getPropertyRegistrationPaymentQuote(today)).thenReturn(refreshedQuote)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(mockPaymentSummaryStep, assertIs<Destination.VisitableStep>(result).step)
        verify(mockState).paymentQuote = refreshedQuote
        verify(mockPaymentService, never()).createPropertyRegistrationPayment(any(), any(), any(), any())
        verifyNoInteractions(mockAbsoluteUrlProvider)
    }

    @Test
    fun `resolveNextDestination refreshes a quote from a previous UK day without creating a payment`() {
        // Arrange
        stubAddressTask()
        stubSummaryDestination()
        whenever(mockAddressTask.isAddressAlreadyRegistered).thenReturn(false)
        whenever(mockState.paymentQuote).thenReturn(createQuote(today.minusDays(1)))
        val refreshedQuote = createQuote(today)
        whenever(mockPaymentService.getPropertyRegistrationPaymentQuote(today)).thenReturn(refreshedQuote)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(mockPaymentSummaryStep, assertIs<Destination.VisitableStep>(result).step)
        verify(mockState).paymentQuote = refreshedQuote
        verify(mockPaymentService, never()).createPropertyRegistrationPayment(any(), any(), any(), any())
        verifyNoInteractions(mockAbsoluteUrlProvider)
    }

    private fun stubAddressTask() {
        whenever(mockState.propertyDetailsTask).thenReturn(mockPropertyDetailsTask)
        whenever(mockPropertyDetailsTask.addressTask).thenReturn(mockAddressTask)
    }

    private fun stubSummaryDestination() {
        whenever(mockState.paymentSummaryStep).thenReturn(mockPaymentSummaryStep)
        whenever(mockPaymentSummaryStep.currentJourneyId).thenReturn(journeyId)
    }

    private fun stubPaymentCreation() {
        val returnUrl = "https://example.test/landlord/register-property/payment-return?journeyId=$journeyId"
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockState.loggedInLandlordEmailAtStartOfJourney).thenReturn("landlord@example.com")
        whenever(mockAbsoluteUrlProvider.buildPropertyRegistrationPaymentReturnUri(journeyId)).thenReturn(URI(returnUrl))
        val quote = createQuote(today)
        whenever(mockState.paymentQuote).thenReturn(quote)
        whenever(mockPaymentService.createPropertyRegistrationPayment(journeyId, returnUrl, "landlord@example.com", quote))
            .thenReturn("https://pay.example.test/next")
    }

    private fun createQuote(quoteDate: LocalDate) =
        PropertyRegistrationPaymentQuote(
            amountInPence = 1234,
            quoteDate = quoteDate,
            renewalDate = LocalDate.of(2028, 3, 1),
        )
}
