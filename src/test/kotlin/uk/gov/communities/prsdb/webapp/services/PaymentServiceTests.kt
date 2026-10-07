package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.config.YamlMessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentFailureType
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperty
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentLinks
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayCreatedPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData.Companion.createPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.MonthDay
import java.util.Optional
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

    @Mock
    private lateinit var mockTransactionManager: PlatformTransactionManager

    @Mock
    private lateinit var mockEntityManager: EntityManager

    @Mock
    private lateinit var mockRegisterProperty: () -> PropertyOwnership

    @Mock
    private lateinit var mockDeleteJourney: () -> Unit

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

    @Nested
    inner class CalculateProRatedFeeInPenceTests {
        @ParameterizedTest
        @MethodSource("uk.gov.communities.prsdb.webapp.services.PaymentServiceTests#provideWorkedExamples")
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
    }

    @Nested
    inner class CreatePropertyRegistrationPaymentTests {
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
            val createdDate = Instant.parse("2026-10-02T11:30:00Z")
            stubGovUkPayCreatePayment(paymentId = "new-payment-id", createdDate = createdDate)
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
            assertEquals(createdDate, payment.paymentCreatedAt)
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
        @MethodSource("uk.gov.communities.prsdb.webapp.services.PaymentServiceTests#provideUnfinishedInProgressPaymentStatuses")
        fun `createPropertyRegistrationPayment cancels an in-progress payment that can be cancelled before creating a new one`(
            paymentStatus: PaymentStatus,
            govUkPayStatus: GovUkPayPaymentStatus,
        ) {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val existingPayment = createPayment(status = paymentStatus, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(existingPayment))
            whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId))
                .thenReturn(createGovUkPayPayment(existingPayment.paymentId, govUkPayStatus))
            whenever(mockPaymentRepository.findStatusByPaymentId(existingPayment.paymentId)).thenReturn(PaymentStatus.CANCELLED)
            stubGovUkPayCreatePayment()

            // Act
            paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email)

            // Assert
            val inOrder = inOrder(mockGovUkPayClient, mockPaymentRepository)
            inOrder.verify(mockGovUkPayClient).getPayment(existingPayment.paymentId)
            inOrder.verify(mockGovUkPayClient).cancelPayment(existingPayment.paymentId)
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(existingPayment.paymentId),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(PaymentStatus.CANCELLED),
                any(),
            )
            inOrder.verify(mockGovUkPayClient).createPayment(any())
            // The loaded payment may be stale, so changing or saving it could overwrite a concurrent finalisation
            assertEquals(paymentStatus, existingPayment.status)
            verify(mockPaymentRepository, never()).save(existingPayment)
        }

        @ParameterizedTest
        @MethodSource("uk.gov.communities.prsdb.webapp.services.PaymentServiceTests#provideUnsuccessfulFinishedGovUkPayStatuses")
        fun `createPropertyRegistrationPayment records the outcome of an in-progress payment that has already finished`(
            govUkPayStatus: GovUkPayPaymentStatus,
            expectedStatus: PaymentStatus,
        ) {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val existingPayment = createPayment(incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(existingPayment))
            whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId))
                .thenReturn(createGovUkPayPayment(existingPayment.paymentId, govUkPayStatus))
            whenever(mockPaymentRepository.findStatusByPaymentId(existingPayment.paymentId)).thenReturn(expectedStatus)
            stubGovUkPayCreatePayment()

            // Act
            paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email)

            // Assert
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(existingPayment.paymentId),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(expectedStatus),
                any(),
            )
        }

        @ParameterizedTest
        @MethodSource("uk.gov.communities.prsdb.webapp.services.PaymentServiceTests#provideUnsuccessfulFinishedGovUkPayStatuses")
        fun `createPropertyRegistrationPayment records the outcome of an in-progress payment that finishes before it can be cancelled`(
            govUkPayStatus: GovUkPayPaymentStatus,
            expectedStatus: PaymentStatus,
        ) {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val existingPayment = createPayment(incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(existingPayment))
            whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId)).thenReturn(
                createGovUkPayPayment(existingPayment.paymentId, GovUkPayPaymentStatus.STARTED),
                createGovUkPayPayment(existingPayment.paymentId, govUkPayStatus),
            )
            doThrow(GovUkPayException(HttpStatus.BAD_REQUEST, "P0501", "Cancellation of payment failed", RuntimeException()))
                .whenever(mockGovUkPayClient)
                .cancelPayment(existingPayment.paymentId)
            whenever(mockPaymentRepository.findStatusByPaymentId(existingPayment.paymentId)).thenReturn(expectedStatus)
            stubGovUkPayCreatePayment()

            // Act
            paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email)

            // Assert
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(existingPayment.paymentId),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(expectedStatus),
                any(),
            )
            verify(mockGovUkPayClient).createPayment(any())
        }

        @Test
        fun `createPropertyRegistrationPayment does not record an in-progress status for a payment that cannot be cancelled`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val existingPayment = createPayment(incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(existingPayment))
            whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId)).thenReturn(
                createGovUkPayPayment(existingPayment.paymentId, GovUkPayPaymentStatus.CAPTURABLE).copy(links = GovUkPayPaymentLinks()),
            )
            whenever(mockPaymentRepository.findStatusByPaymentId(existingPayment.paymentId)).thenReturn(PaymentStatus.CREATED)
            stubGovUkPayCreatePayment()

            // Act
            paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email)

            // Assert
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
            verify(mockGovUkPayClient).createPayment(any())
        }

        @Test
        fun `createPropertyRegistrationPayment throws a failed cancellation with context when the payment can still be cancelled`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val existingPayment = createPayment(incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(existingPayment))
            whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId))
                .thenReturn(createGovUkPayPayment(existingPayment.paymentId, GovUkPayPaymentStatus.STARTED))
            val cancellationFailure =
                GovUkPayException(HttpStatus.INTERNAL_SERVER_ERROR, "P0598", "Downstream system error", RuntimeException())
            doThrow(cancellationFailure).whenever(mockGovUkPayClient).cancelPayment(existingPayment.paymentId)

            // Act
            val exception =
                assertThrows<GovUkPayException> { paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email) }

            // Assert
            assertSame(cancellationFailure, exception.cause)
            assertContains(exception.message!!, existingPayment.paymentId)
            assertContains(exception.message!!, journeyId)
            verify(mockGovUkPayClient, never()).createPayment(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
            verify(mockPaymentRepository, never()).save(any<Payment>())
        }

        @Test
        fun `createPropertyRegistrationPayment records an in-progress payment that has already succeeded and does not create another`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val existingPayment = createPayment(incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(existingPayment))
            whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId))
                .thenReturn(createGovUkPayPayment(existingPayment.paymentId, GovUkPayPaymentStatus.SUCCESS))
            whenever(mockPaymentRepository.findStatusByPaymentId(existingPayment.paymentId)).thenReturn(PaymentStatus.SUCCEEDED)

            // Act
            val exception =
                assertThrows<IllegalStateException> { paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email) }

            // Assert
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(existingPayment.paymentId),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(PaymentStatus.SUCCEEDED),
                any(),
            )
            assertContains(exception.message!!, existingPayment.paymentId)
            assertContains(exception.message!!, journeyId)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockGovUkPayClient, never()).createPayment(any())
        }

        @Test
        fun `createPropertyRegistrationPayment records a payment that succeeds before it can be cancelled and does not create another`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val existingPayment = createPayment(status = PaymentStatus.CAPTURABLE, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(existingPayment))
            whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId)).thenReturn(
                createGovUkPayPayment(existingPayment.paymentId, GovUkPayPaymentStatus.CAPTURABLE),
                createGovUkPayPayment(existingPayment.paymentId, GovUkPayPaymentStatus.SUCCESS),
            )
            doThrow(GovUkPayException(HttpStatus.BAD_REQUEST, "P0501", "Cancellation of payment failed", RuntimeException()))
                .whenever(mockGovUkPayClient)
                .cancelPayment(existingPayment.paymentId)
            whenever(mockPaymentRepository.findStatusByPaymentId(existingPayment.paymentId)).thenReturn(PaymentStatus.SUCCEEDED)

            // Act
            val exception =
                assertThrows<IllegalStateException> { paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email) }

            // Assert
            assertContains(exception.message!!, existingPayment.paymentId)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(existingPayment.paymentId),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(PaymentStatus.SUCCEEDED),
                any(),
            )
            verify(mockGovUkPayClient, never()).createPayment(any())
        }

        @Test
        fun `createPropertyRegistrationPayment does not create another payment when the property already has a succeeded payment`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val succeededPayment = createPayment(status = PaymentStatus.SUCCEEDED, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(succeededPayment))

            // Act
            val exception =
                assertThrows<IllegalStateException> { paymentService.createPropertyRegistrationPayment(journeyId, returnUrl, email) }

            // Assert
            assertContains(exception.message!!, succeededPayment.paymentId)
            assertContains(exception.message!!, journeyId)
            verifyNoInteractions(mockGovUkPayClient)
            verify(mockPaymentRepository, never()).save(any<Payment>())
        }
    }

    @Nested
    inner class GetGovUkPayPaymentStatusTests {
        @ParameterizedTest
        @MethodSource("uk.gov.communities.prsdb.webapp.services.PaymentServiceTests#provideGovUkPayStatusesAndExpectedStatuses")
        fun `getGovUkPayPaymentStatus maps the GovUkPay payment status to a payment status`(
            govUkPayStatus: GovUkPayPaymentStatus,
            expectedStatus: PaymentStatus,
        ) {
            // Arrange
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(PAYMENT_ID, govUkPayStatus))

            // Act
            val paymentStatusCheck = paymentService.getGovUkPayPaymentStatus(PAYMENT_ID)

            // Assert
            assertEquals(PAYMENT_ID, paymentStatusCheck.paymentId)
            assertEquals(expectedStatus, paymentStatusCheck.status)
        }

        @Test
        fun `getGovUkPayPaymentStatus marks the payment as cancellable when GOV UK Pay returns a cancel link`() {
            // Arrange
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.STARTED))

            // Act
            val paymentStatusCheck = paymentService.getGovUkPayPaymentStatus(PAYMENT_ID)

            // Assert
            assertTrue(paymentStatusCheck.isCancellable)
        }

        @Test
        fun `getGovUkPayPaymentStatus marks the payment as not cancellable when GOV UK Pay returns no cancel link`() {
            // Arrange
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.STARTED).copy(links = GovUkPayPaymentLinks()))

            // Act
            val paymentStatusCheck = paymentService.getGovUkPayPaymentStatus(PAYMENT_ID)

            // Assert
            assertFalse(paymentStatusCheck.isCancellable)
        }

        @ParameterizedTest
        @MethodSource(
            "uk.gov.communities.prsdb.webapp.services.PaymentServiceTests#provideFailedGovUkPayStatusesCodesAndExpectedFailureTypes",
        )
        fun `getGovUkPayPaymentStatus maps the GovUkPay error code to a failure type for failed or cancelled payments`(
            govUkPayStatus: GovUkPayPaymentStatus,
            govUkPayCode: String?,
            expectedStatus: PaymentStatus,
            expectedFailureType: PaymentFailureType,
        ) {
            // Arrange
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, govUkPayStatus, code = govUkPayCode))

            // Act
            val paymentStatusCheck = paymentService.getGovUkPayPaymentStatus(PAYMENT_ID)

            // Assert
            assertEquals(
                PaymentStatusCheckDataModel(PAYMENT_ID, expectedStatus, isCancellable = false, failureType = expectedFailureType),
                paymentStatusCheck,
            )
        }

        @ParameterizedTest
        @EnumSource(GovUkPayPaymentStatus::class, names = ["CREATED", "STARTED", "SUBMITTED", "CAPTURABLE", "SUCCESS"])
        fun `getGovUkPayPaymentStatus does not set a failure type for payments that have not failed or been cancelled`(
            govUkPayStatus: GovUkPayPaymentStatus,
        ) {
            // Arrange
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, govUkPayStatus, code = "P0010"))

            // Act
            val paymentStatusCheck = paymentService.getGovUkPayPaymentStatus(PAYMENT_ID)

            // Assert
            assertNull(paymentStatusCheck.failureType)
        }

        @Test
        fun `getGovUkPayPaymentStatus propagates GovUkPayException from the client`() {
            // Arrange
            val govUkPayException = GovUkPayException("GovUkPay request failed: connection refused")
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenThrow(govUkPayException)

            // Act
            val thrownException = assertThrows<GovUkPayException> { paymentService.getGovUkPayPaymentStatus(PAYMENT_ID) }

            // Assert
            assertSame(govUkPayException, thrownException)
        }
    }

    @Nested
    inner class FinalisePaymentTests {
        @Test
        fun `finalisePayment throws when GOV UK Pay reports the payment is still in progress`() {
            // Act, Assert
            assertThrows<IllegalArgumentException> {
                paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CREATED, mockRegisterProperty, mockDeleteJourney)
            }
            verifyNoInteractions(mockPaymentRepository, mockGovUkPayClient, mockRegisterProperty, mockDeleteJourney)
        }

        @ParameterizedTest
        @EnumSource(PaymentStatus::class, names = ["FAILED", "CANCELLED"])
        fun `finalisePayment sets an unsuccessful status if the payment is in progress and returns the current status`(
            govUkPayStatus: PaymentStatus,
        ) {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(govUkPayStatus)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, govUkPayStatus, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(govUkPayStatus, status)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(govUkPayStatus),
                any(),
            )
            verifyNoInteractions(mockGovUkPayClient, mockRegisterProperty, mockDeleteJourney)
        }

        @Test
        fun `finalisePayment returns the current status without updating it when GOV UK Pay reports the payment succeeded`() {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
            verifyNoInteractions(mockGovUkPayClient, mockRegisterProperty, mockDeleteJourney)
        }

        @Test
        fun `finalisePayment throws when the payment does not exist`() {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(null)

            // Act, Assert
            assertThrows<IllegalStateException> {
                paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, mockRegisterProperty, mockDeleteJourney)
            }
        }

        @Test
        fun `finalisePayment returns the current status without side effects when the payment cannot be claimed`() {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.CREATED)),
                eq(PaymentStatus.CAPTURABLE),
                any(),
            )
            verifyNoInteractions(mockTransactionManager, mockGovUkPayClient, mockRegisterProperty, mockDeleteJourney)
        }

        @Test
        fun `finalisePayment registers the property, links it to the payment and captures the payment once claimed`() {
            // Arrange
            // Deliberately stale, as the managed entity may have been loaded before the payment was claimed
            val payment = createPayment(status = PaymentStatus.CREATED)
            val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
            val property = MockLandlordData.createPropertyOwnership(renewalDate = LocalDate.of(2027, 6, 1))
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockRegisterProperty.invoke()).thenReturn(property)
            stubIncompletePropertyLock(payment)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            assertEquals(PaymentStatus.SUCCEEDED, payment.status)
            assertEquals(property, payment.associatedProperty)
            assertNull(payment.associatedIncompleteProperty)
            assertEquals(property.renewalDate, payment.forPeriodEnding)
            val inOrder =
                inOrder(
                    mockPaymentRepository,
                    mockRegisterProperty,
                    mockEntityManager,
                    mockDeleteJourney,
                    mockGovUkPayClient,
                    mockTransactionManager,
                )
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.CREATED)),
                eq(PaymentStatus.CAPTURABLE),
                any(),
            )
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.CAPTURABLE)),
                eq(PaymentStatus.SUCCEEDED),
                any(),
            )
            inOrder.verify(mockRegisterProperty).invoke()
            inOrder.verify(mockPaymentRepository).saveAndFlush(payment)
            inOrder.verify(mockEntityManager).detach(incompleteProperty)
            inOrder.verify(mockDeleteJourney).invoke()
            inOrder.verify(mockPaymentRepository).flush()
            inOrder.verify(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            inOrder.verify(mockTransactionManager).commit(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @Test
        fun `finalisePayment does not register the property if the payment is no longer capturable when finalising`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubTransaction()
            stubIncompletePropertyLock()
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verifyNoInteractions(mockRegisterProperty, mockDeleteJourney, mockGovUkPayClient)
            verify(mockPaymentRepository, never()).findAllByAssociatedIncompleteProperty(any())
        }

        @Test
        fun `finalisePayment rolls back, cancels the payment and returns the current status when registration fails`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubIncompletePropertyLock()
            whenever(mockRegisterProperty.invoke()).thenThrow(EntityExistsException("Address already registered"))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockTransactionManager, never()).commit(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            val inOrder = inOrder(mockGovUkPayClient, mockPaymentRepository)
            inOrder.verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.CAPTURABLE)),
                eq(PaymentStatus.CANCELLED),
                any(),
            )
        }

        @Test
        fun `finalisePayment rolls back, cancels the payment and returns the current status when capture fails`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockRegisterProperty.invoke()).thenReturn(MockLandlordData.createPropertyOwnership())
            stubIncompletePropertyLock()
            doThrow(GovUkPayException("Capture failed")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.CAPTURABLE)),
                eq(PaymentStatus.CANCELLED),
                any(),
            )
        }

        @Test
        fun `finalisePayment throws and leaves the payment capturable when cancelling the payment fails`() {
            // Arrange
            val registrationException = EntityExistsException("Address already registered")
            val cancellationFailure = GovUkPayException("Cancel failed")
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubIncompletePropertyLock()
            whenever(mockRegisterProperty.invoke()).thenThrow(registrationException)
            doThrow(cancellationFailure).whenever(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.CAPTURABLE))

            // Act
            val exception =
                assertThrows<GovUkPayException> {
                    paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)
                }

            // Assert
            assertSame(cancellationFailure, exception.cause)
            assertEquals(registrationException, exception.suppressed.single())
            // Only the claim and the rolled-back finalisation update the status
            verify(mockPaymentRepository, times(2)).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        }

        @ParameterizedTest
        @MethodSource("uk.gov.communities.prsdb.webapp.services.PaymentServiceTests#provideUnsuccessfulFinishedGovUkPayStatuses")
        fun `finalisePayment records the outcome of a payment that finishes before it can be cancelled`(
            govUkPayStatus: GovUkPayPaymentStatus,
            expectedStatus: PaymentStatus,
        ) {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubIncompletePropertyLock()
            whenever(mockRegisterProperty.invoke()).thenThrow(EntityExistsException("Address already registered"))
            doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(PAYMENT_ID, govUkPayStatus))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(expectedStatus)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(expectedStatus, status)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.CAPTURABLE)),
                eq(expectedStatus),
                any(),
            )
        }

        @Test
        fun `finalisePayment throws and leaves the payment capturable when GOV UK Pay has taken a payment that could not be finalised`() {
            // Arrange
            val captureException = GovUkPayException("Capture timed out")
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockRegisterProperty.invoke()).thenReturn(MockLandlordData.createPropertyOwnership())
            stubIncompletePropertyLock()
            doThrow(captureException).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS))

            // Act
            val exception =
                assertThrows<IllegalStateException> {
                    paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)
                }

            // Assert
            assertSame(captureException, exception.cause)
            verify(mockTransactionManager).rollback(any())
            // Only the claim and the rolled-back finalisation update the status
            verify(mockPaymentRepository, times(2)).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        }

        @Test
        fun `finalisePayment rolls back and cancels the payment without capturing when registration marks the transaction for rollback`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
            val transactionStatus = stubTransaction()
            whenever(mockRegisterProperty.invoke()).thenAnswer {
                transactionStatus.setRollbackOnly()
                MockLandlordData.createPropertyOwnership()
            }
            stubIncompletePropertyLock()
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.CAPTURABLE)),
                eq(PaymentStatus.CANCELLED),
                any(),
            )
        }

        @Test
        fun `finalisePayment rolls back and cancels the payment without capturing it when deleting the journey fails`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubIncompletePropertyLock()
            whenever(mockRegisterProperty.invoke()).thenReturn(MockLandlordData.createPropertyOwnership())
            whenever(mockDeleteJourney.invoke()).thenThrow(IllegalStateException("Journey could not be deleted"))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.CAPTURABLE)),
                eq(PaymentStatus.CANCELLED),
                any(),
            )
        }

        @Test
        fun `finalisePayment cancels the property's other in-progress payments, and cancels them on GOV UK Pay once committed`() {
            // Arrange
            val payment = createPayment(paymentId = PAYMENT_ID)
            val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
            val inProgressPayment = createPayment(paymentId = OTHER_PAYMENT_ID, incompleteProperty = incompleteProperty)
            val failedPayment =
                createPayment(paymentId = FINISHED_PAYMENT_ID, status = PaymentStatus.FAILED, incompleteProperty = incompleteProperty)
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubIncompletePropertyLock(payment)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(payment, inProgressPayment, failedPayment))
            whenever(mockPaymentRepository.findStatusByPaymentId(OTHER_PAYMENT_ID)).thenReturn(PaymentStatus.CREATED)
            whenever(mockPaymentRepository.findStatusByPaymentId(FINISHED_PAYMENT_ID)).thenReturn(PaymentStatus.FAILED)
            whenever(
                mockPaymentRepository.updateStatusIfCurrentStatusIn(
                    eq(OTHER_PAYMENT_ID),
                    eq(PaymentStatus.IN_PROGRESS_STATUSES),
                    eq(PaymentStatus.CANCELLED),
                    any(),
                ),
            ).thenReturn(1)
            whenever(mockRegisterProperty.invoke()).thenReturn(MockLandlordData.createPropertyOwnership())

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            val inOrder =
                inOrder(
                    mockLandlordIncompletePropertiesRepository,
                    mockPaymentRepository,
                    mockRegisterProperty,
                    mockGovUkPayClient,
                    mockTransactionManager,
                )
            inOrder.verify(mockLandlordIncompletePropertiesRepository).findByIdForUpdate(incompleteProperty.id)
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.CAPTURABLE)),
                eq(PaymentStatus.SUCCEEDED),
                any(),
            )
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(OTHER_PAYMENT_ID),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(PaymentStatus.CANCELLED),
                any(),
            )
            inOrder.verify(mockRegisterProperty).invoke()
            inOrder.verify(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            inOrder.verify(mockTransactionManager).commit(any())
            inOrder.verify(mockGovUkPayClient).cancelPayment(OTHER_PAYMENT_ID)
            verify(mockGovUkPayClient, never()).cancelPayment(FINISHED_PAYMENT_ID)
            verify(mockGovUkPayClient, never()).cancelPayment(PAYMENT_ID)
        }

        @Test
        fun `finalisePayment cancels the payment without registering the property when another payment for the property has succeeded`() {
            // Arrange
            val payment = createPayment(paymentId = PAYMENT_ID)
            val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
            val succeededPayment =
                createPayment(paymentId = OTHER_PAYMENT_ID, status = PaymentStatus.SUCCEEDED, incompleteProperty = incompleteProperty)
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubIncompletePropertyLock(payment)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(payment, succeededPayment))
            whenever(mockPaymentRepository.findStatusByPaymentId(OTHER_PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verifyNoInteractions(mockRegisterProperty, mockDeleteJourney)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            verify(mockGovUkPayClient, never()).cancelPayment(OTHER_PAYMENT_ID)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(eq(OTHER_PAYMENT_ID), any(), any(), any())
        }

        @Test
        fun `finalisePayment cancels the payment without updating it when its incomplete property no longer exists`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubTransaction()
            whenever(mockPaymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(createPayment(paymentId = PAYMENT_ID)))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verifyNoInteractions(mockRegisterProperty, mockDeleteJourney)
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), eq(PaymentStatus.SUCCEEDED), any())
        }

        @Test
        fun `finalisePayment still succeeds when another payment for the property cannot be cancelled on GOV UK Pay`() {
            // Arrange
            val payment = createPayment(paymentId = PAYMENT_ID)
            val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
            val inProgressPayment = createPayment(paymentId = OTHER_PAYMENT_ID, incompleteProperty = incompleteProperty)
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
            stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubIncompletePropertyLock(payment)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(payment, inProgressPayment))
            whenever(mockPaymentRepository.findStatusByPaymentId(OTHER_PAYMENT_ID)).thenReturn(PaymentStatus.CAPTURABLE)
            whenever(
                mockPaymentRepository.updateStatusIfCurrentStatusIn(
                    eq(OTHER_PAYMENT_ID),
                    eq(PaymentStatus.IN_PROGRESS_STATUSES),
                    eq(PaymentStatus.CANCELLED),
                    any(),
                ),
            ).thenReturn(1)
            whenever(mockRegisterProperty.invoke()).thenReturn(MockLandlordData.createPropertyOwnership())
            doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment(OTHER_PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(OTHER_PAYMENT_ID)).thenThrow(GovUkPayException("GOV.UK Pay unavailable"))

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty, mockDeleteJourney)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verify(mockTransactionManager).commit(any())
            verify(mockGovUkPayClient).cancelPayment(OTHER_PAYMENT_ID)
            verify(mockGovUkPayClient, never()).cancelPayment(PAYMENT_ID)
        }
    }

    @Nested
    inner class GetPropertyRegistrationPaymentStatusTests {
        @Test
        fun `getPropertyRegistrationPaymentStatus returns the GOV UK Pay status of the latest payment for the journey`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val latestPayment = createPayment(paymentId = PAYMENT_ID, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findFirstByAssociatedIncompletePropertyOrderByPaymentCreatedAtDesc(incompleteProperty))
                .thenReturn(latestPayment)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS))

            // Act
            val paymentStatusCheck = paymentService.getPropertyRegistrationPaymentStatus(journeyId)

            // Assert
            assertEquals(PaymentStatusCheckDataModel(PAYMENT_ID, PaymentStatus.SUCCEEDED, isCancellable = false), paymentStatusCheck)
        }

        @Test
        fun `getPropertyRegistrationPaymentStatus throws when there is no incomplete property for the journey`() {
            // Arrange
            setMockPrincipal(userId)
            whenever(mockLandlordIncompletePropertiesRepository.findBySavedJourneyState_JourneyIdAndUser_Id(journeyId, userId))
                .thenReturn(null)

            // Act, Assert
            assertThrows<IllegalStateException> { paymentService.getPropertyRegistrationPaymentStatus(journeyId) }
            verifyNoInteractions(mockGovUkPayClient, mockPaymentRepository)
        }

        @Test
        fun `getPropertyRegistrationPaymentStatus throws when the journey has no payment`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            whenever(mockPaymentRepository.findFirstByAssociatedIncompletePropertyOrderByPaymentCreatedAtDesc(incompleteProperty))
                .thenReturn(null)

            // Act
            val exception = assertThrows<IllegalStateException> { paymentService.getPropertyRegistrationPaymentStatus(journeyId) }

            // Assert
            assertContains(exception.message!!, journeyId)
            verifyNoInteractions(mockGovUkPayClient)
        }
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
        TransactionTemplate(mockTransactionManager),
        mockEntityManager,
    )

    private fun setUpIncompletePropertyAndLandlord(anniversary: MonthDay = MonthDay.of(Month.MARCH, 1)): LandlordIncompleteProperty {
        val incompleteProperty = setUpIncompleteProperty()
        val landlord =
            MockLandlordData.createIndividualLandlord(baseUser = incompleteProperty.user).apply { setAnniversaryIfAbsent(anniversary) }
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
        return incompleteProperty
    }

    private fun setUpIncompleteProperty(): LandlordIncompleteProperty {
        setMockPrincipal(userId)
        val user = MockLandlordData.createPrsdbUser(userId)
        val incompleteProperty =
            LandlordIncompleteProperty(user, MockSavedJourneyStateData.createSavedJourneyState(journeyId = journeyId, baseUser = user))
        whenever(mockLandlordIncompletePropertiesRepository.findBySavedJourneyState_JourneyIdAndUser_Id(journeyId, userId))
            .thenReturn(incompleteProperty)
        return incompleteProperty
    }

    private fun stubGovUkPayCreatePayment(
        paymentId: String = "new-payment-id",
        nextUrl: String = "https://pay.example.test/next",
        createdDate: Instant = Instant.now(),
    ) {
        // GOV.UK Pay echoes the requested amount and reference back in the created payment
        whenever(mockGovUkPayClient.createPayment(any())).thenAnswer { invocation ->
            val request = invocation.getArgument<GovUkPayCreatePaymentRequest>(0)
            createGovUkPayCreatedPayment(
                paymentId = paymentId,
                amount = request.amount,
                reference = request.reference,
                createdDate = createdDate,
                nextUrl = nextUrl,
            )
        }
    }

    private fun setMockPrincipal(name: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }

    private fun stubConditionalUpdate(
        expectedStatuses: List<PaymentStatus>,
        newStatus: PaymentStatus,
    ) {
        whenever(
            mockPaymentRepository.updateStatusIfCurrentStatusIn(eq(PAYMENT_ID), eq(expectedStatuses), eq(newStatus), any()),
        ).thenReturn(1)
    }

    private fun stubIncompletePropertyLock(payment: Payment = createPayment(paymentId = PAYMENT_ID)) {
        val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
        whenever(mockPaymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment))
        whenever(mockLandlordIncompletePropertiesRepository.findByIdForUpdate(incompleteProperty.id)).thenReturn(incompleteProperty)
    }

    private fun stubTransaction(): TransactionStatus {
        val transactionStatus = SimpleTransactionStatus()
        whenever(mockTransactionManager.getTransaction(anyOrNull())).thenReturn(transactionStatus)
        return transactionStatus
    }

    companion object {
        private const val PAYMENT_ID = "payment-id"
        private const val OTHER_PAYMENT_ID = "other-payment-id"
        private const val FINISHED_PAYMENT_ID = "finished-payment-id"

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
        fun provideUnsuccessfulFinishedGovUkPayStatuses() =
            listOf(
                Arguments.of(GovUkPayPaymentStatus.FAILED, PaymentStatus.FAILED),
                Arguments.of(GovUkPayPaymentStatus.ERROR, PaymentStatus.FAILED),
                Arguments.of(GovUkPayPaymentStatus.CANCELLED, PaymentStatus.CANCELLED),
            )

        @JvmStatic
        fun provideGovUkPayStatusesAndExpectedStatuses() =
            listOf(
                Arguments.of(GovUkPayPaymentStatus.CREATED, PaymentStatus.CREATED),
                Arguments.of(GovUkPayPaymentStatus.STARTED, PaymentStatus.CREATED),
                Arguments.of(GovUkPayPaymentStatus.SUBMITTED, PaymentStatus.CREATED),
                Arguments.of(GovUkPayPaymentStatus.CAPTURABLE, PaymentStatus.CAPTURABLE),
                Arguments.of(GovUkPayPaymentStatus.SUCCESS, PaymentStatus.SUCCEEDED),
                Arguments.of(GovUkPayPaymentStatus.FAILED, PaymentStatus.FAILED),
                Arguments.of(GovUkPayPaymentStatus.CANCELLED, PaymentStatus.CANCELLED),
                Arguments.of(GovUkPayPaymentStatus.ERROR, PaymentStatus.FAILED),
            )

        @JvmStatic
        fun provideFailedGovUkPayStatusesCodesAndExpectedFailureTypes() =
            listOf(
                Arguments.of(GovUkPayPaymentStatus.FAILED, "P0010", PaymentStatus.FAILED, PaymentFailureType.PAYMENT_METHOD_REJECTED),
                Arguments.of(GovUkPayPaymentStatus.FAILED, "P0020", PaymentStatus.FAILED, PaymentFailureType.PAYMENT_EXPIRED),
                Arguments.of(GovUkPayPaymentStatus.FAILED, "P0030", PaymentStatus.FAILED, PaymentFailureType.CANCELLED_BY_USER),
                Arguments.of(GovUkPayPaymentStatus.CANCELLED, "P0040", PaymentStatus.CANCELLED, PaymentFailureType.CANCELLED_BY_SERVICE),
                Arguments.of(GovUkPayPaymentStatus.ERROR, "P0050", PaymentStatus.FAILED, PaymentFailureType.PAYMENT_PROVIDER_ERROR),
                Arguments.of(GovUkPayPaymentStatus.FAILED, "P9999", PaymentStatus.FAILED, PaymentFailureType.UNKNOWN),
                Arguments.of(GovUkPayPaymentStatus.FAILED, null, PaymentStatus.FAILED, PaymentFailureType.UNKNOWN),
            )
    }
}
