package uk.gov.communities.prsdb.webapp.journeys.shared.payment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito.lenient
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
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PaymentState
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.PaymentService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.test.assertIs

@ExtendWith(MockitoExtension::class)
class PaymentSummaryStepConfigTests {
    @Mock
    private lateinit var mockPaymentService: PaymentService

    @Mock
    private lateinit var mockState: PaymentState

    @Mock
    private lateinit var mockDependencies: PaymentDependencies

    @Mock
    private lateinit var mockPaymentHandler: PaymentHandler

    @Mock
    private lateinit var mockPaymentSummaryStep: PaymentSummaryStep

    private lateinit var stepConfig: PaymentSummaryStepConfig

    private val journeyId = "journey-123"
    private val defaultDestination = Destination.ExternalUrl("default")
    private val today = LocalDate.now(DateTimeHelper.UK_ZONE)

    @BeforeEach
    fun setUp() {
        stepConfig = PaymentSummaryStepConfig(mockPaymentService)
        stepConfig.urlPath = PaymentSummaryStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        lenient().`when`(mockState.dependencies).thenReturn(mockDependencies)
        lenient().`when`(mockDependencies.paymentHandler).thenReturn(mockPaymentHandler)
    }

    @Test
    fun `getStepSpecificContent stores a newly calculated quote in journey state`() {
        // Arrange
        val quote = createQuote(today)
        stubSummaryContent(quote, firstChargeableDate = LocalDate.of(2027, 1, 1))

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        verify(mockState).paymentQuote = quote
        assertEquals("£20", content["annualFee"])
        assertEquals("1 January 2027", content["firstChargeableDate"])
        assertEquals(today.toString(), content["quoteDate"])
        assertEquals(
            listOf(
                SummaryListRowViewModel("registerProperty.paymentSummary.summary.address", listOf("1 Example Road", "EG1 2AB")),
                SummaryListRowViewModel("registerProperty.paymentSummary.summary.payment", "£12.34"),
                SummaryListRowViewModel(
                    "registerProperty.paymentSummary.summary.paymentDate",
                    today.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)),
                ),
                SummaryListRowViewModel("registerProperty.paymentSummary.summary.renewalDate", "1 March 2028"),
            ),
            content["summaryListRows"],
        )
    }

    @Test
    fun `getStepSpecificContent flags the pre-gratis period when the first chargeable date is after the quote date`() {
        // Arrange
        stubSummaryContent(createQuote(today), firstChargeableDate = today.plusDays(1))

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(true, content["isPreGratisPeriod"])
    }

    @Test
    fun `getStepSpecificContent does not flag the pre-gratis period when charging starts on the quote date`() {
        // Arrange
        stubSummaryContent(createQuote(today), firstChargeableDate = today)

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(false, content["isPreGratisPeriod"])
    }

    @Test
    fun `getStepSpecificContent uses the payment start date for the first chargeable date`() {
        // Arrange
        val paymentStartDate = today.plusDays(30)
        val quote = createQuote(today).copy(paymentStartDate = paymentStartDate)
        whenever(mockPaymentHandler.getQuote(any())).thenReturn(quote)
        whenever(mockPaymentService.getAnnualFeeInPence()).thenReturn(2000)
        whenever(mockPaymentService.getFirstChargeableDate(paymentStartDate)).thenReturn(paymentStartDate)
        whenever(mockDependencies.paymentSummaryAddressLines).thenReturn(listOf("1 Example Road"))

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(false, content["isPreGratisPeriod"])
        assertEquals(paymentStartDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)), content["firstChargeableDate"])
    }

    @Test
    fun `chooseTemplate selects the payment summary template`() {
        assertEquals("forms/paymentSummary", stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `afterStepDataIsAdded clears the previous payment outcome and reference`() {
        // Arrange
        whenever(mockPaymentHandler.isPropertyAlreadyRegistered()).thenReturn(false)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockState).paymentOutcome = null
        verify(mockState).paymentReference = null
    }

    @Test
    fun `afterStepDataIsAdded marks the property as already registered when the address has been registered since the journey started`() {
        // Arrange
        whenever(mockPaymentHandler.isPropertyAlreadyRegistered()).thenReturn(true)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockDependencies).isPropertyAlreadyRegistered = true
    }

    @Test
    fun `afterStepDataIsAdded does not mark the property as already registered when the address is still available`() {
        // Arrange
        whenever(mockPaymentHandler.isPropertyAlreadyRegistered()).thenReturn(false)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockDependencies, never()).isPropertyAlreadyRegistered = anyOrNull()
    }

    @Test
    fun `resolveNextDestination redirects to the already registered step when the property is already registered`() {
        // Arrange
        whenever(mockDependencies.isPropertyAlreadyRegistered).thenReturn(true)
        val mockAlreadyRegisteredStep = mock<RequestableStep<*, *, *>>()
        whenever(mockAlreadyRegisteredStep.currentJourneyId).thenReturn(journeyId)
        whenever(mockDependencies.alreadyRegisteredStep).thenReturn(mockAlreadyRegisteredStep)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(mockAlreadyRegisteredStep, assertIs<Destination.VisitableStep>(result).step)
        verifyNoInteractions(mockPaymentService, mockPaymentHandler)
        verify(mockState, never()).paymentQuote
    }

    @Test
    fun `resolveNextDestination creates a payment and redirects to GOV UK Pay when the property is not already registered`() {
        // Arrange
        whenever(mockDependencies.isPropertyAlreadyRegistered).thenReturn(false)
        stubPaymentCreation()

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals("https://pay.example.test/next", assertIs<Destination.ExternalUrl>(result).externalUrl)
        verify(mockPaymentHandler).createPayment(createQuote(today))
        verify(mockPaymentHandler, never()).getQuote(any())
        verify(mockState, never()).paymentQuote = anyOrNull()
    }

    @Test
    fun `resolveNextDestination returns to the payment summary when another payment was started at the same time`() {
        // Arrange
        stubSummaryDestination()
        whenever(mockDependencies.isPropertyAlreadyRegistered).thenReturn(false)
        stubPaymentCreation(PaymentCreationResult.ConcurrentPaymentStarted)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(mockPaymentSummaryStep, assertIs<Destination.VisitableStep>(result).step)
    }

    @Test
    fun `resolveNextDestination refreshes a missing quote without creating a payment`() {
        // Arrange
        stubSummaryDestination()
        whenever(mockDependencies.isPropertyAlreadyRegistered).thenReturn(false)
        stubSubmittedQuoteDate(today.toString())
        val refreshedQuote = createQuote(today)
        whenever(mockPaymentHandler.getQuote(today)).thenReturn(refreshedQuote)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(mockPaymentSummaryStep, assertIs<Destination.VisitableStep>(result).step)
        verify(mockState).paymentQuote = refreshedQuote
        verify(mockPaymentHandler, never()).createPayment(any())
    }

    @Test
    fun `resolveNextDestination refreshes a quote from a previous UK day without creating a payment`() {
        // Arrange
        stubSummaryDestination()
        whenever(mockDependencies.isPropertyAlreadyRegistered).thenReturn(false)
        whenever(mockState.paymentQuote).thenReturn(createQuote(today.minusDays(1)))
        stubSubmittedQuoteDate(today.toString())
        val refreshedQuote = createQuote(today)
        whenever(mockPaymentHandler.getQuote(today)).thenReturn(refreshedQuote)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(mockPaymentSummaryStep, assertIs<Destination.VisitableStep>(result).step)
        verify(mockState).paymentQuote = refreshedQuote
        verify(mockPaymentHandler, never()).createPayment(any())
    }

    @ParameterizedTest
    @MethodSource("provideStaleSubmittedQuoteDates")
    fun `resolveNextDestination refreshes the quote without creating a payment when the submitted quote date is not today`(
        submittedQuoteDate: String?,
    ) {
        // Arrange
        stubSummaryDestination()
        whenever(mockDependencies.isPropertyAlreadyRegistered).thenReturn(false)
        whenever(mockState.paymentQuote).thenReturn(createQuote(today))
        stubSubmittedQuoteDate(submittedQuoteDate)
        val refreshedQuote = createQuote(today)
        whenever(mockPaymentHandler.getQuote(today)).thenReturn(refreshedQuote)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        assertEquals(mockPaymentSummaryStep, assertIs<Destination.VisitableStep>(result).step)
        verify(mockState).paymentQuote = refreshedQuote
        verify(mockPaymentHandler, never()).createPayment(any())
    }

    private fun stubSubmittedQuoteDate(quoteDate: String?) {
        whenever(mockState.getStepData(PaymentSummaryStep.ROUTE_SEGMENT))
            .thenReturn(quoteDate?.let { mapOf("quoteDate" to it) } ?: emptyMap())
    }

    private fun stubSummaryContent(
        quote: PropertyPaymentQuote,
        firstChargeableDate: LocalDate,
    ) {
        whenever(mockDependencies.paymentSummaryAddressLines).thenReturn(listOf("1 Example Road", "EG1 2AB"))
        whenever(mockPaymentHandler.getQuote(any())).thenReturn(quote)
        whenever(mockPaymentService.getAnnualFeeInPence()).thenReturn(2000)
        whenever(mockPaymentService.getFirstChargeableDate(quote.paymentStartDate)).thenReturn(firstChargeableDate)
    }

    private fun stubSummaryDestination() {
        whenever(mockState.paymentSummaryStep).thenReturn(mockPaymentSummaryStep)
        whenever(mockPaymentSummaryStep.currentJourneyId).thenReturn(journeyId)
    }

    private fun stubPaymentCreation(
        result: PaymentCreationResult =
            PaymentCreationResult.NewPayment("https://pay.example.test/next"),
    ) {
        val quote = createQuote(today)
        whenever(mockState.paymentQuote).thenReturn(quote)
        stubSubmittedQuoteDate(today.toString())
        whenever(mockPaymentHandler.createPayment(quote)).thenReturn(result)
    }

    private fun createQuote(quoteDate: LocalDate) =
        PropertyPaymentQuote(
            amountInPence = 1234,
            quoteDate = quoteDate,
            renewalDate = LocalDate.of(2028, 3, 1),
        )

    companion object {
        @JvmStatic
        fun provideStaleSubmittedQuoteDates() =
            listOf(
                Arguments.of(LocalDate.now(DateTimeHelper.UK_ZONE).minusDays(1).toString()),
                Arguments.of("not-a-date"),
                Arguments.of(null),
            )
    }
}
