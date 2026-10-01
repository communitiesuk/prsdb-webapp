package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.config.YamlMessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperty
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatedPayment
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPayment
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentState
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.MonthDay
import java.util.UUID
import kotlin.test.assertContains

@ExtendWith(MockitoExtension::class)
class PaymentServiceTests {
    @Mock
    private lateinit var mockGovUkPayClient: GovUkPayClient

    @Mock
    private lateinit var mockPaymentRepository: PaymentRepository

    @Mock
    private lateinit var mockLandlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository

    @Mock
    private lateinit var mockUserToLandlordService: UserToLandlordService

    private val messageSource = YamlMessageSource("classpath:messages")

    private lateinit var paymentService: PaymentService

    private val journeyId = "journey-123"
    private val userId = "user-123"
    private val returnUrl = "https://example.test/landlord/register-property/payment-return?journeyId=journey-123"
    private val email = "landlord@example.com"
    private val pastGratisPeriodEndDate = "2020-01-01"

    @BeforeEach
    fun setUp() {
        paymentService = createPaymentService()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @ParameterizedTest
    @MethodSource("provideWorkedExamples")
    fun `calculateProRatedFeeInPence matches external worked examples`(
        annualFeeInPence: Int,
        today: LocalDate,
        renewalDate: LocalDate,
        expectedFeeInPence: Int,
    ) {
        // Arrange
        val paymentServiceWithPolicyFee = createPaymentService(annualFeeInPence = annualFeeInPence)

        // Act
        val fee = paymentServiceWithPolicyFee.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        assertEquals(expectedFeeInPence, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence only charges for days after the gratis period when today is before it ends`() {
        // Arrange
        val today = LocalDate.of(2027, 6, 1)
        val renewalDate = LocalDate.of(2028, 1, 15)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 228, g = 167, d = 365 -> 334.25
        assertEquals(334, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence treats 14 November 2027 as a gratis day`() {
        // Arrange
        val today = LocalDate.of(2027, 11, 14)
        val renewalDate = LocalDate.of(2028, 2, 1)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 79, g = 1, d = 365 -> 427.40
        assertEquals(427, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence has no gratis days when today is after the gratis period`() {
        // Arrange
        val today = LocalDate.of(2027, 11, 15)
        val renewalDate = LocalDate.of(2028, 2, 1)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 78, g = 0, d = 365 -> 427.40
        assertEquals(427, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence returns zero when the renewal date is before the end of the gratis period`() {
        // Arrange
        val today = LocalDate.of(2026, 12, 1)
        val renewalDate = LocalDate.of(2027, 6, 1)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 182, g = 349, d = 365 -> negative, clamped to 0
        assertEquals(0, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence returns zero when the renewal date is the day after the gratis period ends`() {
        // Arrange
        val today = LocalDate.of(2026, 11, 15)
        val renewalDate = LocalDate.of(2027, 11, 15)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 365, g = 365, d = 365 -> 0
        assertEquals(0, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence uses 366 days when 29 February falls within the chargeable period`() {
        // Arrange
        val today = LocalDate.of(2027, 12, 1)
        val renewalDate = LocalDate.of(2028, 6, 1)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 183, g = 0, d = 366 -> 1000.00 (would be 1002 with d = 365)
        assertEquals(1000, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence uses 365 days when 29 February is the renewal date`() {
        // Arrange
        val today = LocalDate.of(2027, 12, 1)
        val renewalDate = LocalDate.of(2028, 2, 29)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 90, g = 0, d = 365 -> 493.15 (would be 491 with d = 366)
        assertEquals(493, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence uses 366 days when today is 29 February`() {
        // Arrange
        val today = LocalDate.of(2028, 2, 29)
        val renewalDate = LocalDate.of(2029, 1, 1)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 307, g = 0, d = 366 -> 1677.60 (would be 1682 with d = 365)
        assertEquals(1677, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence applies both gratis days and 366 days in year when both are relevant`() {
        // Arrange
        val today = LocalDate.of(2027, 10, 1)
        val renewalDate = LocalDate.of(2028, 3, 1)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 152, g = 45, d = 366 -> 584.70 (would be 586 with d = 365)
        assertEquals(584, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence returns the full annual fee for a full non-leap year after the gratis period`() {
        // Arrange
        val today = LocalDate.of(2029, 3, 1)
        val renewalDate = LocalDate.of(2030, 3, 1)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 365, g = 0, d = 365 -> 2000
        assertEquals(2000, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence returns the full annual fee for a full leap year after the gratis period`() {
        // Arrange
        val today = LocalDate.of(2028, 1, 1)
        val renewalDate = LocalDate.of(2029, 1, 1)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 366, g = 0, d = 366 -> 2000
        assertEquals(2000, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence charges for a single day when the renewal date is tomorrow`() {
        // Arrange
        val today = LocalDate.of(2027, 11, 15)
        val renewalDate = LocalDate.of(2027, 11, 16)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 1, g = 0, d = 365 -> 5.48
        assertEquals(5, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence returns zero when today is 14 November 2027 and the renewal date is tomorrow`() {
        // Arrange
        val today = LocalDate.of(2027, 11, 14)
        val renewalDate = LocalDate.of(2027, 11, 15)

        // Act
        val fee = paymentService.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 1, g = 1, d = 365 -> 0
        assertEquals(0, fee)
    }

    @Test
    fun `calculateProRatedFeeInPence throws when the renewal date is today`() {
        // Arrange
        val today = LocalDate.of(2028, 1, 1)

        // Act, Assert
        assertThrows<IllegalArgumentException> {
            paymentService.calculateProRatedFeeInPence(today, today)
        }
    }

    @Test
    fun `calculateProRatedFeeInPence throws when the renewal date is before today`() {
        // Arrange
        val today = LocalDate.of(2028, 1, 1)
        val renewalDate = LocalDate.of(2027, 12, 31)

        // Act, Assert
        assertThrows<IllegalArgumentException> {
            paymentService.calculateProRatedFeeInPence(renewalDate, today)
        }
    }

    @Test
    fun `calculateProRatedFeeInPence uses the configured gratis period end date`() {
        // Arrange
        val paymentServiceWithEarlierGratisPeriod = createPaymentService(gratisPeriodEndDate = "2026-01-31")
        val today = LocalDate.of(2026, 1, 1)
        val renewalDate = LocalDate.of(2027, 1, 1)

        // Act
        val fee = paymentServiceWithEarlierGratisPeriod.calculateProRatedFeeInPence(renewalDate, today)

        // Assert
        // c = 365, g = 31, d = 365 -> 1830.14
        assertEquals(1830, fee)
    }

    @Test
    fun `createPropertyRegistrationPayment requests a GOV UK Pay payment for the pro-rated fee`() {
        // Arrange
        paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
        val anniversary = MonthDay.of(Month.MARCH, 1)
        setUpIncompletePropertyAndLandlord(anniversary)
        stubGovUkPayCreatePayment()
        val expectedAmount = paymentService.calculateProRatedFeeInPence(RenewalDateHelper.getRenewalDate(anniversary))

        // Act
        paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email)

        // Assert
        val requestCaptor = argumentCaptor<GovUkPayCreatePaymentRequest>()
        verify(mockGovUkPayClient).createPayment(requestCaptor.capture())
        val request = requestCaptor.firstValue
        assertEquals(expectedAmount, request.amount)
        assertEquals("Register your rental property", request.description)
        assertEquals(returnUrl, request.returnUrl)
        assertEquals(email, request.email)
        assertDoesNotThrow { UUID.fromString(request.reference) }
    }

    @Test
    fun `createPropertyRegistrationPayment saves a created payment for the journey and user`() {
        // Arrange
        paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
        val anniversary = MonthDay.of(Month.MARCH, 1)
        val incompleteProperty = setUpIncompletePropertyAndLandlord(anniversary)
        stubGovUkPayCreatePayment(paymentId = "new-payment-id")
        val expectedRenewalDate = RenewalDateHelper.getRenewalDate(anniversary)

        // Act
        paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email)

        // Assert
        val requestCaptor = argumentCaptor<GovUkPayCreatePaymentRequest>()
        verify(mockGovUkPayClient).createPayment(requestCaptor.capture())
        val paymentCaptor = argumentCaptor<Payment>()
        verify(mockPaymentRepository).save(paymentCaptor.capture())
        val payment = paymentCaptor.firstValue
        assertEquals("new-payment-id", payment.paymentId)
        assertEquals(requestCaptor.firstValue.amount, payment.amountInPence)
        assertEquals(requestCaptor.firstValue.reference, payment.reference)
        assertEquals(expectedRenewalDate, payment.forPeriodEnding)
        assertEquals(PaymentStatus.CREATED, payment.status)
        assertEquals(incompleteProperty, payment.associatedIncompleteProperty)
        assertEquals(incompleteProperty.user, payment.payingUser)
    }

    @Test
    fun `createPropertyRegistrationPayment returns the GOV UK Pay next url`() {
        // Arrange
        paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
        setUpIncompletePropertyAndLandlord()
        stubGovUkPayCreatePayment(nextUrl = "https://pay.example.test/next")

        // Act
        val nextUrl = paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email)

        // Assert
        assertEquals("https://pay.example.test/next", nextUrl)
    }

    @Test
    fun `createPropertyRegistrationPayment throws when there is no incomplete property for the journey`() {
        // Arrange
        paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
        setMockPrincipal(userId)
        whenever(mockLandlordIncompletePropertiesRepository.findBySavedJourneyState_JourneyIdAndUser_Id(journeyId, userId))
            .thenReturn(null)

        // Act & Assert
        assertThrows<IllegalStateException> { paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email) }
        verifyNoInteractions(mockGovUkPayClient, mockPaymentRepository)
    }

    @Test
    fun `createPropertyRegistrationPayment throws without calling GOV UK Pay when the fee is zero`() {
        // Arrange
        paymentService = createPaymentService(gratisPeriodEndDate = "2099-01-01")
        setUpIncompletePropertyAndLandlord()

        // Act
        val exception =
            assertThrows<IllegalStateException> { paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email) }

        // Assert
        assertContains(exception.message!!, "GOV.UK Pay only accepts amounts greater than zero")
        verifyNoInteractions(mockGovUkPayClient, mockPaymentRepository)
    }

    @Test
    fun `createPropertyRegistrationPayment does not save a payment when GOV UK Pay fails`() {
        // Arrange
        paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
        setUpIncompletePropertyAndLandlord()
        whenever(mockGovUkPayClient.createPayment(any())).thenThrow(GovUkPayException("GOV.UK Pay request failed", RuntimeException()))

        // Act & Assert
        assertThrows<GovUkPayException> { paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email) }
        verify(mockPaymentRepository, never()).save(any<Payment>())
    }

    @ParameterizedTest
    @MethodSource("provideUnfinishedInProgressPaymentStatuses")
    fun `createPropertyRegistrationPayment cancels an unfinished in-progress payment before creating a new one`(
        paymentStatus: PaymentStatus,
        govUkPayStatus: GovUkPayPaymentStatus,
    ) {
        // Arrange
        paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
        val incompleteProperty = setUpIncompletePropertyAndLandlord()
        val existingPayment = createExistingPayment(incompleteProperty, paymentStatus)
        whenever(mockPaymentRepository.findAllByAssociatedIncompletePropertyAndStatusIn(incompleteProperty, inProgressPaymentStatuses))
            .thenReturn(listOf(existingPayment))
        whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId))
            .thenReturn(createGovUkPayPayment(existingPayment.paymentId, govUkPayStatus))
        stubGovUkPayCreatePayment()

        // Act
        paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email)

        // Assert
        val inOrder = inOrder(mockGovUkPayClient)
        inOrder.verify(mockGovUkPayClient).cancelPayment(existingPayment.paymentId)
        inOrder.verify(mockGovUkPayClient).createPayment(any())
        assertEquals(PaymentStatus.CANCELLED, existingPayment.status)
        verify(mockPaymentRepository).save(existingPayment)
    }

    @ParameterizedTest
    @MethodSource("provideFinishedGovUkPayStatuses")
    fun `createPropertyRegistrationPayment records the outcome of an in-progress payment that has already finished`(
        govUkPayStatus: GovUkPayPaymentStatus,
        expectedStatus: PaymentStatus,
    ) {
        // Arrange
        paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
        val incompleteProperty = setUpIncompletePropertyAndLandlord()
        val existingPayment = createExistingPayment(incompleteProperty)
        whenever(mockPaymentRepository.findAllByAssociatedIncompletePropertyAndStatusIn(incompleteProperty, inProgressPaymentStatuses))
            .thenReturn(listOf(existingPayment))
        whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId))
            .thenReturn(createGovUkPayPayment(existingPayment.paymentId, govUkPayStatus))
        stubGovUkPayCreatePayment()

        // Act
        paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email)

        // Assert
        verify(mockGovUkPayClient, never()).cancelPayment(any())
        assertEquals(expectedStatus, existingPayment.status)
        verify(mockPaymentRepository).save(existingPayment)
    }

    private fun createPaymentService(
        annualFeeInPence: Int = 2000,
        gratisPeriodEndDate: String = "2027-11-14",
    ) = PaymentService(
        annualFeeInPence,
        gratisPeriodEndDate,
        mockGovUkPayClient,
        mockPaymentRepository,
        mockLandlordIncompletePropertiesRepository,
        mockUserToLandlordService,
        messageSource,
    )

    private fun setUpIncompletePropertyAndLandlord(anniversary: MonthDay = MonthDay.of(Month.MARCH, 1)): LandlordIncompleteProperty {
        setMockPrincipal(userId)
        val user = MockLandlordData.createPrsdbUser(userId)
        val incompleteProperty =
            LandlordIncompleteProperty(user, MockSavedJourneyStateData.createSavedJourneyState(journeyId = journeyId, baseUser = user))
        whenever(mockLandlordIncompletePropertiesRepository.findBySavedJourneyState_JourneyIdAndUser_Id(journeyId, userId))
            .thenReturn(incompleteProperty)
        val landlord = MockLandlordData.createIndividualLandlord(baseUser = user).apply { setAnniversaryIfAbsent(anniversary) }
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
        return incompleteProperty
    }

    private fun stubGovUkPayCreatePayment(
        paymentId: String = "new-payment-id",
        nextUrl: String = "https://pay.example.test/next",
    ) {
        whenever(mockGovUkPayClient.createPayment(any())).thenReturn(GovUkPayCreatedPayment(paymentId, nextUrl))
    }

    private fun setMockPrincipal(name: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }

    private fun createExistingPayment(
        incompleteProperty: LandlordIncompleteProperty,
        status: PaymentStatus = PaymentStatus.CREATED,
    ) = Payment(
        paymentId = "existing-payment-id",
        amountInPence = 1000,
        reference = "existing-reference",
        paymentCreatedAt = Instant.now(),
        forPeriodEnding = LocalDate.of(2027, 3, 1),
        status = status,
        incompleteProperty = incompleteProperty,
    )

    private fun createGovUkPayPayment(
        paymentId: String,
        status: GovUkPayPaymentStatus,
    ) = GovUkPayPayment(
        paymentId = paymentId,
        amount = 1000,
        reference = "existing-reference",
        description = "Register your rental property",
        createdDate = Instant.now(),
        state = GovUkPayPaymentState(status = status, finished = status in finishedGovUkPayStatuses),
    )

    companion object {
        private val inProgressPaymentStatuses = listOf(PaymentStatus.CREATED, PaymentStatus.CAPTURABLE)

        private val finishedGovUkPayStatuses =
            listOf(
                GovUkPayPaymentStatus.SUCCESS,
                GovUkPayPaymentStatus.FAILED,
                GovUkPayPaymentStatus.ERROR,
                GovUkPayPaymentStatus.CANCELLED,
            )

        @JvmStatic
        fun provideUnfinishedInProgressPaymentStatuses() =
            listOf(
                Arguments.of(PaymentStatus.CREATED, GovUkPayPaymentStatus.CREATED),
                Arguments.of(PaymentStatus.CREATED, GovUkPayPaymentStatus.STARTED),
                Arguments.of(PaymentStatus.CREATED, GovUkPayPaymentStatus.SUBMITTED),
                Arguments.of(PaymentStatus.CREATED, GovUkPayPaymentStatus.CAPTURABLE),
                Arguments.of(PaymentStatus.CAPTURABLE, GovUkPayPaymentStatus.CAPTURABLE),
            )

        @JvmStatic
        fun provideWorkedExamples() =
            listOf(
                // c = 365, g = 335, d = 365 -> 534.25
                Arguments.of(6500, LocalDate.of(2026, 12, 15), LocalDate.of(2027, 12, 15), 534),
                // c = 365, g = 288, d = 365 -> 1371.23
                Arguments.of(6500, LocalDate.of(2027, 1, 31), LocalDate.of(2028, 1, 31), 1371),
                // c = 366, g = 143, d = 366 -> 3960.38
                Arguments.of(6500, LocalDate.of(2027, 6, 25), LocalDate.of(2028, 6, 25), 3960),
            )

        @JvmStatic
        fun provideFinishedGovUkPayStatuses() =
            listOf(
                Arguments.of(GovUkPayPaymentStatus.SUCCESS, PaymentStatus.SUCCEEDED),
                Arguments.of(GovUkPayPaymentStatus.FAILED, PaymentStatus.FAILED),
                Arguments.of(GovUkPayPaymentStatus.ERROR, PaymentStatus.FAILED),
                Arguments.of(GovUkPayPaymentStatus.CANCELLED, PaymentStatus.CANCELLED),
            )
    }
}
