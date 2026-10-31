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
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.TransactionSystemException
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.config.YamlMessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentFailureType
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperty
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentCaptureOutcomeUnknownException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentNotCapturedException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentQuote
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentLinks
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PaymentRequiresManualInterventionEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayCreatedPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData.Companion.createPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyRegistrationData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.MonthDay
import java.util.Optional
import java.util.UUID
import kotlin.test.assertContains
import kotlin.test.assertIs

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
    private lateinit var mockPropertyRegistrationService: PropertyRegistrationService

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockSavedJourneyStateRepository: SavedJourneyStateRepository

    @Mock
    private lateinit var mockManualInterventionEmailSender: EmailNotificationService<PaymentRequiresManualInterventionEmail>

    private val registrationData = MockPropertyRegistrationData.createPropertyRegistrationDataModel()

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
    inner class PropertyRegistrationPaymentSummaryDetailsTests {
        @Test
        fun `getAnnualFeeInPence returns the configured annual fee`() {
            assertEquals(3000, createPaymentService(annualFeeInPence = 3000).getAnnualFeeInPence())
        }

        @Test
        fun `getFirstChargeableDate returns the day after the gratis period ends when the quote date is within it`() {
            val service = createPaymentService(gratisPeriodEndDate = "2027-11-14")

            assertEquals(LocalDate.of(2027, 11, 15), service.getFirstChargeableDate(LocalDate.of(2026, 12, 16)))
        }

        @Test
        fun `getFirstChargeableDate returns the day after the gratis period ends when the quote date is the last gratis day`() {
            val service = createPaymentService(gratisPeriodEndDate = "2027-11-14")

            assertEquals(LocalDate.of(2027, 11, 15), service.getFirstChargeableDate(LocalDate.of(2027, 11, 14)))
        }

        @Test
        fun `getFirstChargeableDate returns the quote date when it is the day after the gratis period ends`() {
            val service = createPaymentService(gratisPeriodEndDate = "2027-11-14")

            assertEquals(LocalDate.of(2027, 11, 15), service.getFirstChargeableDate(LocalDate.of(2027, 11, 15)))
        }

        @Test
        fun `getFirstChargeableDate returns the quote date when the gratis period has ended`() {
            val service = createPaymentService(gratisPeriodEndDate = "2027-11-14")

            assertEquals(LocalDate.of(2028, 1, 10), service.getFirstChargeableDate(LocalDate.of(2028, 1, 10)))
        }
    }

    @Nested
    inner class PropertyRegistrationPaymentQuoteTests {
        @Test
        fun `getPropertyRegistrationPaymentQuote uses the existing proration rules and quote date`() {
            // Arrange
            val quoteDate = LocalDate.of(2027, 10, 10)
            val anniversary = MonthDay.of(Month.MARCH, 1)
            val user = MockLandlordData.createPrsdbUser(userId)
            val landlord = MockLandlordData.createIndividualLandlord(baseUser = user).apply { setAnniversaryIfAbsent(anniversary) }
            whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            val service = createPaymentService()
            val expectedRenewalDate = RenewalDateHelper.getRenewalDate(anniversary, quoteDate)

            // Act
            val quote = service.getPropertyRegistrationPaymentQuote(quoteDate)

            // Assert
            assertEquals(service.calculateProRatedFeeInPence(expectedRenewalDate, quoteDate), quote.amountInPence)
            assertEquals(quoteDate, quote.quoteDate)
            assertEquals(expectedRenewalDate, quote.renewalDate)
            verifyNoInteractions(mockGovUkPayClient)
        }

        @Test
        fun `getPropertyRegistrationPaymentQuote falls back to the quote date when the landlord has no anniversary`() {
            // Arrange
            val quoteDate = LocalDate.of(2027, 10, 10)
            val user = MockLandlordData.createPrsdbUser(userId)
            val landlord = MockLandlordData.createIndividualLandlord(baseUser = user)
            whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            val service = createPaymentService()
            val expectedRenewalDate = LocalDate.of(2028, 10, 10)

            // Act
            val quote = service.getPropertyRegistrationPaymentQuote(quoteDate)

            // Assert
            assertEquals(expectedRenewalDate, quote.renewalDate)
        }

        @Test
        fun `getPropertyRegistrationPaymentQuote returns the prorated amount after the gratis period`() {
            // Arrange
            val quoteDate = LocalDate.of(2028, 1, 15)
            val landlord = MockLandlordData.createIndividualLandlord().apply { setAnniversaryIfAbsent(MonthDay.of(Month.MARCH, 1)) }
            whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)

            // Act
            val quote = paymentService.getPropertyRegistrationPaymentQuote(quoteDate)

            // Assert
            assertEquals(LocalDate.of(2028, 3, 1), quote.renewalDate)
            assertEquals(251, quote.amountInPence)
            verifyNoInteractions(mockGovUkPayClient, mockPaymentRepository, mockLandlordIncompletePropertiesRepository)
        }

        @Test
        fun `getPropertyRegistrationPaymentQuote defaults to the current UK calendar day`() {
            // Arrange
            val landlord = MockLandlordData.createIndividualLandlord()
            whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            val today = LocalDate.now(DateTimeHelper.UK_ZONE)

            // Act
            val quote = paymentService.getPropertyRegistrationPaymentQuote()

            // Assert
            assertEquals(today, quote.quoteDate)
            assertEquals(RenewalDateHelper.getRenewalDate(MonthDay.from(today), today), quote.renewalDate)
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
            paymentService.createPropertyRegistrationPayment(
                journeyId,
                email,
                paymentService.getPropertyRegistrationPaymentQuote(),
            )

            // Assert
            val requestCaptor = argumentCaptor<GovUkPayCreatePaymentRequest>()
            verify(mockGovUkPayClient).createPayment(requestCaptor.capture())
            val request = requestCaptor.firstValue
            assertEquals(expectedAmount, request.amount)
            assertEquals("Register your rental property", request.description)
            assertEquals(email, request.email)
            assertDoesNotThrow { UUID.fromString(request.reference) }
        }

        @Test
        fun `createPropertyRegistrationPayment uses the supplied journey quote`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            setMockPrincipal(userId)
            val user = MockLandlordData.createPrsdbUser(userId)
            val incompleteProperty =
                LandlordIncompleteProperty(
                    user,
                    MockSavedJourneyStateData.createSavedJourneyState(journeyId = journeyId, baseUser = user),
                )
            whenever(mockLandlordIncompletePropertiesRepository.findBySavedJourneyState_JourneyIdAndUser_Id(journeyId, userId))
                .thenReturn(incompleteProperty)
            val expectedRenewalDate = LocalDate.of(2028, 3, 1)
            val quote =
                PropertyRegistrationPaymentQuote(
                    amountInPence = 1234,
                    quoteDate = LocalDate.of(2027, 10, 10),
                    renewalDate = expectedRenewalDate,
                )
            stubGovUkPayCreatePayment()

            // Act
            paymentService.createPropertyRegistrationPayment(journeyId, email, quote)

            // Assert
            val requestCaptor = argumentCaptor<GovUkPayCreatePaymentRequest>()
            verify(mockGovUkPayClient).createPayment(requestCaptor.capture())
            assertEquals(quote.amountInPence, requestCaptor.firstValue.amount)
            val paymentCaptor = argumentCaptor<Payment>()
            verify(mockPaymentRepository).save(paymentCaptor.capture())
            assertEquals(expectedRenewalDate, paymentCaptor.firstValue.forPeriodEnding)
            assertEquals(quote.amountInPence, paymentCaptor.firstValue.amountInPence)
            verifyNoInteractions(mockUserToLandlordService)
            verify(mockAbsoluteUrlProvider).buildPropertyRegistrationPaymentReturnUri(journeyId, requestCaptor.firstValue.reference)
            assertEquals(returnUrl, requestCaptor.firstValue.returnUrl)
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
            paymentService.createPropertyRegistrationPayment(
                journeyId,
                email,
                paymentService.getPropertyRegistrationPaymentQuote(),
            )

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
        fun `createPropertyRegistrationPayment returns a new payment with the GOV UK Pay next url`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            setUpIncompletePropertyAndLandlord()
            stubGovUkPayCreatePayment(nextUrl = "https://pay.example.test/next")

            // Act
            val result =
                paymentService.createPropertyRegistrationPayment(
                    journeyId,
                    email,
                    paymentService.getPropertyRegistrationPaymentQuote(),
                )

            // Assert
            assertEquals(PropertyRegistrationPaymentCreationResult.NewPayment("https://pay.example.test/next"), result)
        }

        @Test
        fun `createPropertyRegistrationPayment throws when there is no incomplete property for the journey`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            setMockPrincipal(userId)
            whenever(mockLandlordIncompletePropertiesRepository.findBySavedJourneyState_JourneyIdAndUser_Id(journeyId, userId))
                .thenReturn(null)
            whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(MockLandlordData.createIndividualLandlord())

            // Act & Assert
            assertThrows<IllegalStateException> {
                paymentService.createPropertyRegistrationPayment(
                    journeyId,
                    email,
                    paymentService.getPropertyRegistrationPaymentQuote(),
                )
            }
            verifyNoInteractions(mockGovUkPayClient, mockPaymentRepository)
        }

        @Test
        fun `createPropertyRegistrationPayment throws without calling GOV UK Pay when the fee is zero`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = "2099-01-01")
            setUpIncompletePropertyAndLandlord()

            // Act
            val exception =
                assertThrows<IllegalStateException> {
                    paymentService.createPropertyRegistrationPayment(
                        journeyId,
                        email,
                        paymentService.getPropertyRegistrationPaymentQuote(),
                    )
                }

            // Assert
            assertContains(exception.message!!, "GOV.UK Pay only accepts amounts greater than zero")
            verifyNoInteractions(mockGovUkPayClient)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
            verify(mockPaymentRepository, never()).save(any<Payment>())
        }

        @Test
        fun `createPropertyRegistrationPayment resumes a confirmed successful payment when the quote is zero`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = "2099-01-01")
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val existingPayment = createPayment(status = PaymentStatus.INDETERMINATE, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(existingPayment))
            whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId))
                .thenReturn(createGovUkPayPayment(existingPayment.paymentId, GovUkPayPaymentStatus.SUCCESS))

            // Act
            val result =
                paymentService.createPropertyRegistrationPayment(
                    journeyId,
                    email,
                    paymentService.getPropertyRegistrationPaymentQuote(),
                )

            // Assert
            assertEquals(PropertyRegistrationPaymentCreationResult.ExistingPayment(existingPayment.reference), result)
            verify(mockGovUkPayClient, never()).createPayment(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
            verify(mockPaymentRepository, never()).save(any<Payment>())
        }

        @Test
        fun `createPropertyRegistrationPayment resumes an indeterminate payment that GOV UK Pay confirms succeeded`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val existingPayment = createPayment(status = PaymentStatus.INDETERMINATE, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(existingPayment))
            whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId))
                .thenReturn(createGovUkPayPayment(existingPayment.paymentId, GovUkPayPaymentStatus.SUCCESS))

            // Act
            val result =
                paymentService.createPropertyRegistrationPayment(
                    journeyId,
                    email,
                    paymentService.getPropertyRegistrationPaymentQuote(),
                )

            // Assert
            assertEquals(PropertyRegistrationPaymentCreationResult.ExistingPayment(existingPayment.reference), result)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockGovUkPayClient, never()).createPayment(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
            verify(mockPaymentRepository, never()).save(any<Payment>())
        }

        @Test
        fun `createPropertyRegistrationPayment finishes all lookups before reconciling any payment`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val firstPayment =
                createPayment(
                    paymentId = "first-payment",
                    paymentCreatedAt = Instant.parse("2026-01-01T00:00:00Z"),
                    status = PaymentStatus.CREATED,
                    incompleteProperty = incompleteProperty,
                )
            val secondPayment =
                createPayment(
                    paymentId = "second-payment",
                    paymentCreatedAt = Instant.parse("2026-01-02T00:00:00Z"),
                    status = PaymentStatus.INDETERMINATE,
                    incompleteProperty = incompleteProperty,
                )
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(firstPayment, secondPayment))
            whenever(mockGovUkPayClient.getPayment(firstPayment.paymentId))
                .thenReturn(createGovUkPayPayment(firstPayment.paymentId, GovUkPayPaymentStatus.STARTED))
            val lookupFailure = GovUkPayException("GOV.UK Pay lookup failed", RuntimeException())
            whenever(mockGovUkPayClient.getPayment(secondPayment.paymentId)).thenThrow(lookupFailure)

            // Act
            val exception =
                assertThrows<GovUkPayException> {
                    paymentService.createPropertyRegistrationPayment(
                        journeyId,
                        email,
                        paymentService.getPropertyRegistrationPaymentQuote(),
                    )
                }

            // Assert
            assertSame(lookupFailure, exception)
            verify(mockGovUkPayClient, never()).cancelPayment(firstPayment.paymentId)
            verify(mockGovUkPayClient, never()).createPayment(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
            verify(mockPaymentRepository, never()).save(any<Payment>())
        }

        @Test
        fun `createPropertyRegistrationPayment does not save a payment when GOV UK Pay fails`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            setUpIncompletePropertyAndLandlord()
            stubPaymentReturnUrl()
            whenever(mockGovUkPayClient.createPayment(any())).thenThrow(GovUkPayException("GOV.UK Pay request failed", RuntimeException()))

            // Act & Assert
            assertThrows<GovUkPayException> {
                paymentService.createPropertyRegistrationPayment(
                    journeyId,
                    email,
                    paymentService.getPropertyRegistrationPaymentQuote(),
                )
            }
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
            paymentService.createPropertyRegistrationPayment(
                journeyId,
                email,
                paymentService.getPropertyRegistrationPaymentQuote(),
            )

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
            paymentService.createPropertyRegistrationPayment(
                journeyId,
                email,
                paymentService.getPropertyRegistrationPaymentQuote(),
            )

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
            paymentService.createPropertyRegistrationPayment(
                journeyId,
                email,
                paymentService.getPropertyRegistrationPaymentQuote(),
            )

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
            paymentService.createPropertyRegistrationPayment(
                journeyId,
                email,
                paymentService.getPropertyRegistrationPaymentQuote(),
            )

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
                assertThrows<GovUkPayException> {
                    paymentService.createPropertyRegistrationPayment(
                        journeyId,
                        email,
                        paymentService.getPropertyRegistrationPaymentQuote(),
                    )
                }

            // Assert
            assertSame(cancellationFailure, exception.cause)
            assertContains(exception.message!!, existingPayment.paymentId)
            assertContains(exception.message!!, journeyId)
            verify(mockGovUkPayClient, never()).createPayment(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
            verify(mockPaymentRepository, never()).save(any<Payment>())
        }

        @Test
        fun `createPropertyRegistrationPayment requires manual intervention for a succeeded CREATED payment and creates another`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val existingPayment = createPayment(status = PaymentStatus.CREATED, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(existingPayment))
            whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId))
                .thenReturn(createGovUkPayPayment(existingPayment.paymentId, GovUkPayPaymentStatus.SUCCESS))
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION, existingPayment.paymentId)
            whenever(mockPaymentRepository.findById(existingPayment.paymentId)).thenReturn(Optional.of(existingPayment))
            stubGovUkPayCreatePayment(nextUrl = "https://pay.example.test/next")

            // Act
            val result =
                paymentService.createPropertyRegistrationPayment(
                    journeyId,
                    email,
                    paymentService.getPropertyRegistrationPaymentQuote(),
                )

            // Assert
            assertEquals(PropertyRegistrationPaymentCreationResult.NewPayment("https://pay.example.test/next"), result)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), eq(PaymentStatus.SUCCEEDED), any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockPaymentRepository).save(any<Payment>())
            verify(mockManualInterventionEmailSender).sendEmail(eq(MANUAL_INTERVENTION_EMAIL_ADDRESS), any())
        }

        @Test
        fun `createPropertyRegistrationPayment resumes an indeterminate payment that succeeds before it can be cancelled`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val existingPayment = createPayment(status = PaymentStatus.INDETERMINATE, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(existingPayment))
            whenever(mockGovUkPayClient.getPayment(existingPayment.paymentId)).thenReturn(
                createGovUkPayPayment(existingPayment.paymentId, GovUkPayPaymentStatus.CAPTURABLE),
                createGovUkPayPayment(existingPayment.paymentId, GovUkPayPaymentStatus.SUCCESS),
            )
            doThrow(GovUkPayException(HttpStatus.BAD_REQUEST, "P0501", "Cancellation of payment failed", RuntimeException()))
                .whenever(mockGovUkPayClient)
                .cancelPayment(existingPayment.paymentId)
            // Act
            val result =
                paymentService.createPropertyRegistrationPayment(
                    journeyId,
                    email,
                    paymentService.getPropertyRegistrationPaymentQuote(),
                )

            // Assert
            assertEquals(PropertyRegistrationPaymentCreationResult.ExistingPayment(existingPayment.reference), result)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), eq(PaymentStatus.SUCCEEDED), any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), eq(PaymentStatus.MANUAL_INTERVENTION), any())
            verify(mockGovUkPayClient, never()).createPayment(any())
            verify(mockGovUkPayClient, times(2)).getPayment(existingPayment.paymentId)
            verify(mockGovUkPayClient).cancelPayment(existingPayment.paymentId)
        }

        @Test
        fun `createPropertyRegistrationPayment does not create another payment when a succeeded payment is finalised before flagging`() {
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
                assertThrows<IllegalStateException> {
                    paymentService.createPropertyRegistrationPayment(
                        journeyId,
                        email,
                        paymentService.getPropertyRegistrationPaymentQuote(),
                    )
                }

            // Assert
            assertContains(exception.message!!, existingPayment.paymentId)
            verify(mockGovUkPayClient, never()).createPayment(any())
            verifyNoInteractions(mockManualInterventionEmailSender)
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
                assertThrows<IllegalStateException> {
                    paymentService.createPropertyRegistrationPayment(
                        journeyId,
                        email,
                        paymentService.getPropertyRegistrationPaymentQuote(),
                    )
                }

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
        fun `finalisePayment rejects a payment that GOV UK Pay reports as still in progress`() {
            // Act
            val exception =
                assertThrows<IllegalArgumentException> {
                    paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CREATED, registrationData)
                }

            // Assert
            assertEquals("Payment $PAYMENT_ID cannot be finalised while in progress", exception.message)
            verifyNoInteractions(
                mockPaymentRepository,
                mockGovUkPayClient,
                mockPropertyRegistrationService,
                mockSavedJourneyStateRepository,
            )
        }

        @Test
        fun `finalisePayment throws when given a status GOV UK Pay never reports`() {
            // Act, Assert
            assertThrows<IllegalArgumentException> {
                paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.MANUAL_INTERVENTION, registrationData)
            }
            verifyNoInteractions(
                mockPaymentRepository,
                mockGovUkPayClient,
                mockPropertyRegistrationService,
                mockSavedJourneyStateRepository,
            )
        }

        @ParameterizedTest
        @EnumSource(PaymentStatus::class, names = ["FAILED", "CANCELLED"])
        fun `finalisePayment sets an unsuccessful status if the payment is in progress and returns the current status`(
            govUkPayStatus: PaymentStatus,
        ) {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(govUkPayStatus)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, govUkPayStatus, registrationData)

            // Assert
            assertEquals(govUkPayStatus, status)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(govUkPayStatus),
                any(),
            )
            verifyNoInteractions(mockGovUkPayClient, mockPropertyRegistrationService, mockSavedJourneyStateRepository)
        }

        @Test
        fun `finalisePayment requires manual intervention when GOV UK Pay reports a payment stored as CREATED succeeded`() {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CREATED)
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, registrationData)

            // Assert
            assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
            verifyNoInteractions(mockGovUkPayClient, mockPropertyRegistrationService, mockSavedJourneyStateRepository)
        }

        @Test
        fun `finalisePayment registers the property without capturing when GOV UK Pay reports an INDETERMINATE payment succeeded`() {
            // Arrange
            val payment = createPayment(paymentId = PAYMENT_ID, status = PaymentStatus.INDETERMINATE)
            val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
            val property = MockLandlordData.createPropertyOwnership()
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment(payment)
            whenever(mockPropertyRegistrationService.registerProperty(registrationData.copy(renewalDate = payment.forPeriodEnding)))
                .thenReturn(property)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            assertEquals(property, payment.associatedProperty)
            verify(mockSavedJourneyStateRepository).delete(incompleteProperty.savedJourneyState)
            verify(mockTransactionManager).commit(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @Test
        fun `finalisePayment requires manual intervention when an INDETERMINATE payment that succeeded cannot be registered`() {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION)
            stubTransaction()
            stubStoredPayment(createPayment(paymentId = PAYMENT_ID, status = PaymentStatus.INDETERMINATE))
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenThrow(EntityExistsException("Address already registered"))

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, registrationData)

            // Assert
            assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @Test
        fun `finalisePayment returns the current status when another request registers an INDETERMINATE payment that succeeded first`() {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID))
                .thenReturn(PaymentStatus.INDETERMINATE, PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment(createPayment(paymentId = PAYMENT_ID, status = PaymentStatus.INDETERMINATE))

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verifyNoInteractions(mockGovUkPayClient, mockPropertyRegistrationService, mockSavedJourneyStateRepository)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(
                any(),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(PaymentStatus.MANUAL_INTERVENTION),
                any(),
            )
        }

        @ParameterizedTest
        @EnumSource(PaymentStatus::class, names = ["CREATED", "INDETERMINATE", "CAPTURABLE"], mode = EnumSource.Mode.EXCLUDE)
        fun `finalisePayment returns the current status without updating it when GOV UK Pay reports a finished payment succeeded`(
            storedStatus: PaymentStatus,
        ) {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(storedStatus)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, registrationData)

            // Assert
            assertEquals(storedStatus, status)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
            verifyNoInteractions(mockGovUkPayClient, mockPropertyRegistrationService, mockSavedJourneyStateRepository)
        }

        @Test
        fun `finalisePayment returns the current status when a succeeded payment is finalised before it can be flagged`() {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID))
                .thenReturn(PaymentStatus.INDETERMINATE, PaymentStatus.SUCCEEDED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verifyNoInteractions(mockManualInterventionEmailSender)
        }

        @Test
        fun `finalisePayment emails support when a payment requires manual intervention`() {
            // Arrange
            val paymentCreatedAt = Instant.parse("2026-01-01T12:00:00Z")
            val payment =
                createPayment(
                    paymentId = PAYMENT_ID,
                    amountInPence = 2000,
                    reference = PAYMENT_REFERENCE,
                    paymentCreatedAt = paymentCreatedAt,
                )
            stubStoredPayment(payment)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION)

            // Act
            paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, registrationData)

            // Assert
            val emailCaptor = argumentCaptor<PaymentRequiresManualInterventionEmail>()
            verify(mockManualInterventionEmailSender).sendEmail(eq(MANUAL_INTERVENTION_EMAIL_ADDRESS), emailCaptor.capture())
            assertEquals(PAYMENT_ID, emailCaptor.firstValue.paymentId)
            assertEquals(PAYMENT_REFERENCE, emailCaptor.firstValue.paymentReference)
            assertEquals(2000, emailCaptor.firstValue.amountInPence)
            assertEquals(paymentCreatedAt, emailCaptor.firstValue.paymentCreatedAt)
            assertContains(emailCaptor.firstValue.reason, "GOV.UK Pay reports it succeeded")
        }

        @Test
        fun `finalisePayment still requires manual intervention when emailing support fails`() {
            // Arrange
            stubStoredPayment()
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION)
            doThrow(RuntimeException("Notify unavailable")).whenever(mockManualInterventionEmailSender).sendEmail(any(), any())
            val outContent = ByteArrayOutputStream()
            val originalOut = System.out
            System.setOut(PrintStream(outContent))

            try {
                // Act
                val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, registrationData)

                // Assert
                assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
                val output = outContent.toString()
                assertContains(output, "Failed to email support about payment $PAYMENT_ID")
                assertContains(output, "Notify unavailable")
            } finally {
                System.setOut(originalOut)
            }
        }

        @Test
        fun `finalisePayment throws when the payment does not exist`() {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(null)

            // Act, Assert
            assertThrows<IllegalStateException> {
                paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, registrationData)
            }
        }

        @Test
        fun `finalisePayment returns the current status without side effects when the payment cannot be claimed`() {
            // Arrange
            stubTransaction()
            stubStoredPayment()
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.INDETERMINATE)),
                eq(PaymentStatus.SUCCEEDED),
                any(),
            )
            verifyNoInteractions(mockPropertyRegistrationService, mockSavedJourneyStateRepository, mockGovUkPayClient)
            verify(mockPaymentRepository, never()).findAllByAssociatedIncompleteProperty(any())
        }

        @Test
        fun `finalisePayment returns the current status without side effects when the payment has already been finalised`() {
            // Arrange
            val payment = createPayment(paymentId = PAYMENT_ID).apply { associateWithProperty(MockLandlordData.createPropertyOwnership()) }
            stubTransaction()
            stubStoredPayment(payment)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(
                any(),
                eq(listOf(PaymentStatus.INDETERMINATE)),
                any(),
                any(),
            )
            verifyNoInteractions(mockPropertyRegistrationService, mockSavedJourneyStateRepository, mockGovUkPayClient)
        }

        @Test
        fun `finalisePayment cancels the payment on GOV UK Pay when it has been deleted along with its incomplete property`() {
            // Arrange
            stubTransaction()

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verifyNoInteractions(mockPropertyRegistrationService, mockSavedJourneyStateRepository)
        }

        @Test
        fun `finalisePayment registers the property, links it to the payment and captures the payment once claimed`() {
            // Arrange
            // Deliberately stale, as the managed entity may have been loaded before the payment was claimed
            val payment = createPayment(status = PaymentStatus.CREATED)
            val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
            val originalPeriodEnd = payment.forPeriodEnding
            val property = MockLandlordData.createPropertyOwnership(renewalDate = originalPeriodEnd.plusDays(1))
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(registrationData.copy(renewalDate = originalPeriodEnd)))
                .thenReturn(property)
            stubStoredPayment(payment)

            // Act
            val status =
                paymentService.finalisePayment(
                    PAYMENT_ID,
                    PaymentStatus.CAPTURABLE,
                    registrationData.copy(renewalDate = originalPeriodEnd.plusYears(1)),
                )

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            assertEquals(PaymentStatus.SUCCEEDED, payment.status)
            assertEquals(property, payment.associatedProperty)
            assertNull(payment.associatedIncompleteProperty)
            assertEquals(originalPeriodEnd, payment.forPeriodEnding)
            val inOrder =
                inOrder(
                    mockPaymentRepository,
                    mockPropertyRegistrationService,
                    mockEntityManager,
                    mockSavedJourneyStateRepository,
                    mockGovUkPayClient,
                    mockTransactionManager,
                )
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.CREATED)),
                eq(PaymentStatus.INDETERMINATE),
                any(),
            )
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.INDETERMINATE)),
                eq(PaymentStatus.SUCCEEDED),
                any(),
            )
            inOrder.verify(mockPropertyRegistrationService).registerProperty(registrationData.copy(renewalDate = originalPeriodEnd))
            inOrder.verify(mockPaymentRepository).saveAndFlush(payment)
            inOrder.verify(mockEntityManager).detach(incompleteProperty)
            inOrder.verify(mockSavedJourneyStateRepository).delete(incompleteProperty.savedJourneyState)
            inOrder.verify(mockPaymentRepository).flush()
            inOrder.verify(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            inOrder.verify(mockTransactionManager).commit(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @Test
        fun `finalisePayment rolls back, cancels the payment and returns the current status when registration fails`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenThrow(EntityExistsException("Address already registered"))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockTransactionManager, never()).commit(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            val inOrder = inOrder(mockGovUkPayClient, mockPaymentRepository)
            inOrder.verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(PaymentStatus.CANCELLED),
                any(),
            )
        }

        @Test
        fun `finalisePayment logs why finalising failed when it cancels the payment`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenThrow(EntityExistsException("Address already registered"))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)
            val outContent = ByteArrayOutputStream()
            val originalOut = System.out
            System.setOut(PrintStream(outContent))

            try {
                // Act
                paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

                // Assert
                val output = outContent.toString()
                assertTrue(output.contains("Failed to finalise payment $PAYMENT_ID"))
                assertTrue(output.contains("EntityExistsException: Address already registered"))
            } finally {
                System.setOut(originalOut)
            }
        }

        @Test
        fun `finalisePayment commits without cancelling when GOV UK Pay has taken the payment after the capture call fails`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture timed out")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS))

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verify(mockGovUkPayClient, times(1)).capturePayment(PAYMENT_ID)
            verify(mockTransactionManager).commit(any())
            verify(mockTransactionManager, never()).rollback(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @Test
        fun `finalisePayment waits only once before retrying a transient status check failure`() {
            // Arrange
            val paymentServiceWithPaymentStatusRetryDelay = createPaymentService(paymentStatusRetryDelay = PAYMENT_STATUS_RETRY_DELAY)
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            var firstStatusCheckFailedAt = 0L
            var statusRetriedAt = 0L
            doThrow(GovUkPayException("Capture timed out")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenAnswer {
                if (firstStatusCheckFailedAt == 0L) {
                    firstStatusCheckFailedAt = System.nanoTime()
                    throw GovUkPayException("Network error", ResourceAccessException("Connection reset"))
                } else {
                    statusRetriedAt = System.nanoTime()
                    createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS)
                }
            }

            // Act
            val status = paymentServiceWithPaymentStatusRetryDelay.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            assertTrue(Duration.ofNanos(statusRetriedAt - firstStatusCheckFailedAt) >= PAYMENT_STATUS_RETRY_DELAY)
            verify(mockGovUkPayClient, times(2)).getPayment(PAYMENT_ID)
        }

        @Test
        fun `finalisePayment preserves the interrupt flag when the first status check confirms capture succeeded`() {
            // Arrange
            val paymentServiceWithPaymentStatusRetryDelay = createPaymentService(paymentStatusRetryDelay = PAYMENT_STATUS_RETRY_DELAY)
            val captureFailure = GovUkPayException("Capture timed out")
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doAnswer {
                Thread.currentThread().interrupt()
                throw captureFailure
            }.whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS))

            // Act
            var wasInterrupted = false
            val status =
                try {
                    paymentServiceWithPaymentStatusRetryDelay.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)
                } finally {
                    // Clears the flag so it can't leak into other tests
                    wasInterrupted = Thread.interrupted()
                }

            // Assert
            assertTrue(wasInterrupted)
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verify(mockGovUkPayClient).getPayment(PAYMENT_ID)
            verify(mockTransactionManager).commit(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `finalisePayment captures the payment again when GOV UK Pay still reports it capturable after the capture call fails`(
            retryStatusCheck: Boolean,
        ) {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture failed")).doNothing().whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            val statusCheck = whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
            val capturablePayment = createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.CAPTURABLE)
            if (retryStatusCheck) {
                statusCheck
                    .thenThrow(GovUkPayException("Network error", ResourceAccessException("Connection reset")))
                    .thenReturn(capturablePayment)
            } else {
                statusCheck.thenReturn(capturablePayment)
            }

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verify(mockGovUkPayClient, times(2)).capturePayment(PAYMENT_ID)
            verify(mockGovUkPayClient, times(if (retryStatusCheck) 2 else 1)).getPayment(PAYMENT_ID)
            verify(mockTransactionManager).commit(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @Test
        fun `finalisePayment rejects the internal indeterminate status as a GOV UK Pay status`() {
            // Act
            val exception =
                assertThrows<IllegalArgumentException> {
                    paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.INDETERMINATE, registrationData)
                }

            // Assert
            assertEquals(
                "Payment $PAYMENT_ID cannot be finalised with INDETERMINATE: it is not a GOV.UK Pay status",
                exception.message,
            )
            verifyNoInteractions(
                mockPaymentRepository,
                mockGovUkPayClient,
                mockPropertyRegistrationService,
                mockSavedJourneyStateRepository,
            )
        }

        @Test
        fun `finalisePayment does not wait before the first status check after capture fails`() {
            // Arrange
            val paymentStatusRetryDelay = Duration.ofSeconds(5)
            val paymentServiceWithPaymentStatusRetryDelay = createPaymentService(paymentStatusRetryDelay = paymentStatusRetryDelay)
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            var captureFailedAt = 0L
            var statusCheckedAt = 0L
            doAnswer {
                captureFailedAt = System.nanoTime()
                throw GovUkPayException("Capture timed out")
            }.whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenAnswer {
                statusCheckedAt = System.nanoTime()
                createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS)
            }

            // Act
            val status = paymentServiceWithPaymentStatusRetryDelay.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            assertTrue(Duration.ofNanos(statusCheckedAt - captureFailedAt) < paymentStatusRetryDelay)
            verify(mockGovUkPayClient).getPayment(PAYMENT_ID)
            verify(mockTransactionManager).commit(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @ParameterizedTest
        @MethodSource("uk.gov.communities.prsdb.webapp.services.PaymentServiceTests#provideTransientStatusCheckFailures")
        fun `finalisePayment recovers after one transient status check failure`(statusCheckFailure: GovUkPayException) {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture timed out")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenThrow(statusCheckFailure)
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS))

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verify(mockGovUkPayClient, times(2)).getPayment(PAYMENT_ID)
            verify(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            verify(mockTransactionManager).commit(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `finalisePayment preserves failed status checks and requires manual intervention without cancelling`(sameFailure: Boolean) {
            // Arrange
            val captureFailure = GovUkPayException("Capture timed out")
            val firstFailure = GovUkPayException("Network error", ResourceAccessException("Connection reset"))
            val retryFailure =
                if (sameFailure) {
                    firstFailure
                } else {
                    GovUkPayException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        null,
                        null,
                        RuntimeException(),
                    )
                }
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(captureFailure).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenThrow(firstFailure).thenThrow(retryFailure)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
            if (sameFailure) {
                assertTrue(retryFailure.suppressed.isEmpty())
            } else {
                assertSame(firstFailure, retryFailure.suppressed.single())
            }
            verify(mockGovUkPayClient, times(2)).getPayment(PAYMENT_ID)
            verify(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @ParameterizedTest
        @MethodSource("uk.gov.communities.prsdb.webapp.services.PaymentServiceTests#provideNonTransientStatusCheckFailures")
        fun `finalisePayment does not retry non-transient status check failures`(statusCheckFailure: GovUkPayException) {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture timed out")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenThrow(statusCheckFailure)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
            verify(mockGovUkPayClient).getPayment(PAYMENT_ID)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @Test
        fun `finalisePayment does not retry a transient status check failure on an interrupted thread`() {
            // Arrange
            val statusCheckFailure = GovUkPayException("Network error", ResourceAccessException("Connection reset"))
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture timed out")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenAnswer {
                Thread.currentThread().interrupt()
                throw statusCheckFailure
            }

            // Act
            var wasInterrupted = false
            val status =
                try {
                    paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)
                } finally {
                    wasInterrupted = Thread.interrupted()
                }

            // Assert
            assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
            assertTrue(wasInterrupted)
            assertIs<InterruptedException>(statusCheckFailure.suppressed.single())
            verify(mockGovUkPayClient).getPayment(PAYMENT_ID)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @Test
        fun `finalisePayment returns the stored status when another request records the outcome first after capture fails`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            whenever(
                mockPaymentRepository.updateStatusIfCurrentStatusIn(
                    eq(PAYMENT_ID),
                    eq(listOf(PaymentStatus.INDETERMINATE)),
                    eq(PaymentStatus.FAILED),
                    any(),
                ),
            ).thenReturn(0)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture failed")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.FAILED))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockGovUkPayClient).getPayment(PAYMENT_ID)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockTransactionManager).rollback(any())
        }

        @Test
        fun `finalisePayment propagates a status write failure with capture diagnostics when GOV UK Pay reports the payment finished`() {
            // Arrange
            val captureFailure = GovUkPayException("Capture failed")
            val statusWriteFailure = DataAccessResourceFailureException("Database unavailable")
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            whenever(
                mockPaymentRepository.updateStatusIfCurrentStatusIn(
                    eq(PAYMENT_ID),
                    eq(listOf(PaymentStatus.INDETERMINATE)),
                    eq(PaymentStatus.CANCELLED),
                    any(),
                ),
            ).thenThrow(statusWriteFailure)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(captureFailure).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.CANCELLED))

            // Act
            val exception =
                assertThrows<DataAccessResourceFailureException> {
                    paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)
                }

            // Assert
            assertSame(statusWriteFailure, exception)
            val notCapturedException = assertIs<PaymentNotCapturedException>(exception.suppressed.single())
            assertEquals(PaymentStatus.CANCELLED, notCapturedException.govUkPayStatus)
            assertEquals(
                "GOV.UK Pay reports payment $PAYMENT_ID as CANCELLED after it failed to capture",
                notCapturedException.message,
            )
            assertSame(captureFailure, notCapturedException.cause)
            verify(mockGovUkPayClient).getPayment(PAYMENT_ID)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockTransactionManager).rollback(any())
        }

        @Test
        fun `finalisePayment logs the capture failure when GOV UK Pay reports the payment finished`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.FAILED)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture failed")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.FAILED))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.FAILED)
            val outContent = ByteArrayOutputStream()
            val originalOut = System.out
            System.setOut(PrintStream(outContent))

            try {
                // Act
                paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

                // Assert
                val output = outContent.toString()
                assertTrue(output.contains("Failed to capture payment $PAYMENT_ID, so recording its outcome"))
                assertTrue(output.contains("GOV.UK Pay reports payment $PAYMENT_ID as FAILED after it failed to capture"))
                assertTrue(output.contains("GovUkPayException: Capture failed"))
            } finally {
                System.setOut(originalOut)
            }
        }

        @Test
        fun `finalisePayment requires manual intervention and logs why when it cannot confirm GOV UK Pay captured the payment`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture timed out")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenThrow(GovUkPayException("GOV.UK Pay unavailable"))
            val outContent = ByteArrayOutputStream()
            val originalOut = System.out
            System.setOut(PrintStream(outContent))

            try {
                // Act
                val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

                // Assert
                assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
                verify(mockTransactionManager).rollback(any())
                verify(mockGovUkPayClient, never()).cancelPayment(any())
                verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                    eq(PAYMENT_ID),
                    eq(PaymentStatus.IN_PROGRESS_STATUSES),
                    eq(PaymentStatus.MANUAL_INTERVENTION),
                    any(),
                )
                val output = outContent.toString()
                assertContains(output, "Payment requires manual intervention: $PAYMENT_ID")
                assertContains(output, "Capture timed out")
                assertContains(output, "GOV.UK Pay unavailable")
            } finally {
                System.setOut(originalOut)
            }
        }

        @Test
        fun `finalisePayment requires manual intervention when GOV UK Pay keeps rejecting the capture`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture failed")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.CAPTURABLE))

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
            verify(mockGovUkPayClient, times(2)).capturePayment(PAYMENT_ID)
            verify(mockGovUkPayClient, times(1)).getPayment(PAYMENT_ID)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
        }

        @Test
        fun `finalisePayment returns the current status instead of requiring manual intervention when another request finished it`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture timed out")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenThrow(GovUkPayException("GOV.UK Pay unavailable"))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)
            val outContent = ByteArrayOutputStream()
            val originalOut = System.out
            System.setOut(PrintStream(outContent))

            try {
                // Act
                val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

                // Assert
                assertEquals(PaymentStatus.SUCCEEDED, status)
                assertFalse(outContent.toString().contains("Payment requires manual intervention"))
                verifyNoInteractions(mockManualInterventionEmailSender)
            } finally {
                System.setOut(originalOut)
            }
        }

        @Test
        fun `finalisePayment logs and rethrows when it cannot record that the payment requires manual intervention`() {
            // Arrange
            val flagFailure = DataAccessResourceFailureException("Database unavailable")
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            whenever(
                mockPaymentRepository.updateStatusIfCurrentStatusIn(
                    eq(PAYMENT_ID),
                    eq(PaymentStatus.IN_PROGRESS_STATUSES),
                    eq(PaymentStatus.MANUAL_INTERVENTION),
                    any(),
                ),
            ).thenThrow(flagFailure)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture timed out")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenThrow(GovUkPayException("GOV.UK Pay unavailable"))
            val outContent = ByteArrayOutputStream()
            val originalOut = System.out
            System.setOut(PrintStream(outContent))

            try {
                // Act
                val exception =
                    assertThrows<DataAccessResourceFailureException> {
                        paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)
                    }

                // Assert
                assertSame(flagFailure, exception)
                assertIs<PaymentCaptureOutcomeUnknownException>(exception.suppressed.single())
                val output = outContent.toString()
                assertContains(output, "Payment requires manual intervention: $PAYMENT_ID")
                assertContains(output, "but recording this failed")
                verifyNoInteractions(mockManualInterventionEmailSender)
            } finally {
                System.setOut(originalOut)
            }
        }

        @ParameterizedTest
        @MethodSource("uk.gov.communities.prsdb.webapp.services.PaymentServiceTests#provideUnsuccessfulFinishedGovUkPayStatuses")
        fun `finalisePayment rolls back and records known capture failures without cancelling`(
            govUkPayStatus: GovUkPayPaymentStatus,
            expectedStatus: PaymentStatus,
        ) {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), expectedStatus)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture failed")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(PAYMENT_ID, govUkPayStatus))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(expectedStatus)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(expectedStatus, status)
            verify(mockGovUkPayClient, times(1)).capturePayment(PAYMENT_ID)
            verify(mockGovUkPayClient, times(1)).getPayment(PAYMENT_ID)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockTransactionManager).rollback(any())
            verify(mockTransactionManager, never()).commit(any())
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.INDETERMINATE)),
                eq(expectedStatus),
                any(),
            )
        }

        @Test
        fun `finalisePayment throws and leaves the payment indeterminate when cancelling the payment fails`() {
            // Arrange
            val registrationException = EntityExistsException("Address already registered")
            val cancellationFailure = GovUkPayException("Cancel failed")
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment()
            whenever(mockPropertyRegistrationService.registerProperty(any())).thenThrow(registrationException)
            doThrow(cancellationFailure).whenever(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.CAPTURABLE))

            // Act
            val exception =
                assertThrows<GovUkPayException> {
                    paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)
                }

            // Assert
            assertSame(cancellationFailure, exception.cause)
            assertEquals(registrationException, exception.suppressed.single())
            // Only recording the payment as indeterminate and the rolled-back claim update the status
            verify(mockPaymentRepository, times(2)).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        }

        @ParameterizedTest
        @MethodSource("uk.gov.communities.prsdb.webapp.services.PaymentServiceTests#provideUnsuccessfulFinishedGovUkPayStatuses")
        fun `finalisePayment records the outcome of a payment that finishes before it can be cancelled`(
            govUkPayStatus: GovUkPayPaymentStatus,
            expectedStatus: PaymentStatus,
        ) {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenThrow(EntityExistsException("Address already registered"))
            doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(PAYMENT_ID, govUkPayStatus))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(expectedStatus)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(expectedStatus, status)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(expectedStatus),
                any(),
            )
        }

        @Test
        fun `finalisePayment registers the property again without capturing when the transaction fails to commit after the capture`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubFailingCommits("Commit failed")

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verify(mockGovUkPayClient, times(1)).capturePayment(PAYMENT_ID)
            verify(mockPropertyRegistrationService, times(2)).registerProperty(any())
            verify(mockTransactionManager, times(2)).commit(any())
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockGovUkPayClient, never()).getPayment(any())
        }

        @Test
        fun `finalisePayment requires manual intervention and logs both failures when registering a captured payment fails again`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubFailingCommits("First commit failed", "Second commit failed")
            val outContent = ByteArrayOutputStream()
            val originalOut = System.out
            System.setOut(PrintStream(outContent))

            try {
                // Act
                val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

                // Assert
                assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
                verify(mockGovUkPayClient, times(1)).capturePayment(PAYMENT_ID)
                verify(mockGovUkPayClient, never()).cancelPayment(any())
                verify(mockGovUkPayClient, never()).getPayment(any())
                val alert = outContent.toString().substringAfter("Payment requires manual intervention: $PAYMENT_ID")
                assertContains(alert, "Second commit failed")
                assertContains(alert, "First commit failed")
            } finally {
                System.setOut(originalOut)
            }
        }

        @Test
        fun `finalisePayment returns the current status when another request finishes a captured payment before it is registered again`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            whenever(
                mockPaymentRepository.updateStatusIfCurrentStatusIn(
                    eq(PAYMENT_ID),
                    eq(listOf(PaymentStatus.INDETERMINATE)),
                    eq(PaymentStatus.SUCCEEDED),
                    any(),
                ),
            ).thenReturn(1, 0)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubFailingCommits("Commit failed")
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.MANUAL_INTERVENTION)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.MANUAL_INTERVENTION, status)
            verify(mockPropertyRegistrationService, times(1)).registerProperty(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(
                any(),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(PaymentStatus.MANUAL_INTERVENTION),
                any(),
            )
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verifyNoInteractions(mockManualInterventionEmailSender)
        }

        @Test
        fun `finalisePayment returns succeeded when another request finalises the payment while this one is cancelling it`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenThrow(EntityExistsException("Address already registered"))
            doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS))
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            // Only recording the payment as indeterminate and the rolled-back claim update the status
            verify(mockPaymentRepository, times(2)).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        }

        @Test
        fun `finalisePayment rolls back and cancels the payment without capturing when registration marks the transaction for rollback`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            val transactionStatus = stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer {
                transactionStatus.setRollbackOnly()
                MockLandlordData.createPropertyOwnership()
            }
            stubStoredPayment()
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(PaymentStatus.CANCELLED),
                any(),
            )
        }

        @Test
        fun `finalisePayment rolls back and cancels the payment without capturing it when deleting the journey fails`() {
            // Arrange
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            doThrow(IllegalStateException("Journey could not be deleted")).whenever(mockSavedJourneyStateRepository).delete(any())
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(PaymentStatus.CANCELLED),
                any(),
            )
        }

        @Test
        fun `finalisePayment cancels the property's other in-progress payments on GOV UK Pay, then records them as cancelled`() {
            // Arrange
            val payment = createPayment(paymentId = PAYMENT_ID)
            val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
            val inProgressPayment = createPayment(paymentId = OTHER_PAYMENT_ID, incompleteProperty = incompleteProperty)
            val failedPayment =
                createPayment(paymentId = FINISHED_PAYMENT_ID, status = PaymentStatus.FAILED, incompleteProperty = incompleteProperty)
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment(payment)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(payment, inProgressPayment, failedPayment))
            whenever(mockPaymentRepository.findStatusByPaymentId(OTHER_PAYMENT_ID)).thenReturn(PaymentStatus.CREATED)
            whenever(mockPaymentRepository.findStatusByPaymentId(FINISHED_PAYMENT_ID)).thenReturn(PaymentStatus.FAILED)
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            val inOrder =
                inOrder(
                    mockPaymentRepository,
                    mockPropertyRegistrationService,
                    mockGovUkPayClient,
                    mockTransactionManager,
                )
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.INDETERMINATE)),
                eq(PaymentStatus.SUCCEEDED),
                any(),
            )
            inOrder.verify(mockGovUkPayClient).cancelPayment(OTHER_PAYMENT_ID)
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(OTHER_PAYMENT_ID),
                eq(PaymentStatus.IN_PROGRESS_STATUSES),
                eq(PaymentStatus.CANCELLED),
                any(),
            )
            inOrder.verify(mockPropertyRegistrationService).registerProperty(registrationData.copy(renewalDate = payment.forPeriodEnding))
            inOrder.verify(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            inOrder.verify(mockTransactionManager).commit(any())
            verify(mockGovUkPayClient, never()).cancelPayment(FINISHED_PAYMENT_ID)
            verify(mockGovUkPayClient, never()).cancelPayment(PAYMENT_ID)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(eq(FINISHED_PAYMENT_ID), any(), any(), any())
        }

        @Test
        fun `finalisePayment cancels the payment without registering the property when another payment for the property has succeeded`() {
            // Arrange
            val payment = createPayment(paymentId = PAYMENT_ID)
            val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
            val succeededPayment =
                createPayment(paymentId = OTHER_PAYMENT_ID, status = PaymentStatus.SUCCEEDED, incompleteProperty = incompleteProperty)
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment(payment)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(payment, succeededPayment))
            whenever(mockPaymentRepository.findStatusByPaymentId(OTHER_PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verifyNoInteractions(mockPropertyRegistrationService, mockSavedJourneyStateRepository)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            verify(mockGovUkPayClient, never()).cancelPayment(OTHER_PAYMENT_ID)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(eq(OTHER_PAYMENT_ID), any(), any(), any())
        }

        @Test
        fun `finalisePayment cancels the payment without registering the property when another payment cannot be cancelled`() {
            // Arrange
            val payment = createPayment(paymentId = PAYMENT_ID)
            val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
            val inProgressPayment = createPayment(paymentId = OTHER_PAYMENT_ID, incompleteProperty = incompleteProperty)
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment(payment)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(payment, inProgressPayment))
            whenever(mockPaymentRepository.findStatusByPaymentId(OTHER_PAYMENT_ID)).thenReturn(PaymentStatus.INDETERMINATE)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)
            doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment(OTHER_PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(OTHER_PAYMENT_ID)).thenThrow(GovUkPayException("GOV.UK Pay unavailable"))

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockTransactionManager, never()).commit(any())
            verifyNoInteractions(mockPropertyRegistrationService, mockSavedJourneyStateRepository)
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(eq(OTHER_PAYMENT_ID), any(), any(), any())
        }

        @Test
        fun `finalisePayment cancels the payment without registering the property when GOV UK Pay has taken another payment`() {
            // Arrange
            val payment = createPayment(paymentId = PAYMENT_ID)
            val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
            val inProgressPayment = createPayment(paymentId = OTHER_PAYMENT_ID, incompleteProperty = incompleteProperty)
            stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.INDETERMINATE)
            stubConditionalUpdate(listOf(PaymentStatus.INDETERMINATE), PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment(payment)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(payment, inProgressPayment))
            whenever(mockPaymentRepository.findStatusByPaymentId(OTHER_PAYMENT_ID)).thenReturn(PaymentStatus.INDETERMINATE)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)
            doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment(OTHER_PAYMENT_ID)
            whenever(mockGovUkPayClient.getPayment(OTHER_PAYMENT_ID))
                .thenReturn(createGovUkPayPayment(OTHER_PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS))

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockTransactionManager, never()).commit(any())
            verifyNoInteractions(mockPropertyRegistrationService, mockSavedJourneyStateRepository)
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockGovUkPayClient).cancelPayment(PAYMENT_ID)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(eq(OTHER_PAYMENT_ID), any(), any(), any())
        }
    }

    @Nested
    inner class FindRecoverablePaymentReferenceTests {
        @Test
        fun `returns the oldest INDETERMINATE payment GOV UK Pay reports succeeded after checking every INDETERMINATE payment`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val oldestPayment =
                createPayment(
                    paymentId = "oldest-payment-id",
                    reference = "oldest-reference",
                    paymentCreatedAt = Instant.parse("2025-01-01T00:00:00Z"),
                    status = PaymentStatus.INDETERMINATE,
                    incompleteProperty = incompleteProperty,
                )
            val newerPayment =
                createPayment(
                    paymentId = "newer-payment-id",
                    reference = "newer-reference",
                    paymentCreatedAt = Instant.parse("2025-01-02T00:00:00Z"),
                    status = PaymentStatus.INDETERMINATE,
                    incompleteProperty = incompleteProperty,
                )
            val manualInterventionPayment =
                createPayment(
                    paymentId = "manual-payment-id",
                    status = PaymentStatus.MANUAL_INTERVENTION,
                    incompleteProperty = incompleteProperty,
                )
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(newerPayment, manualInterventionPayment, oldestPayment))
            whenever(mockGovUkPayClient.getPayment(oldestPayment.paymentId))
                .thenReturn(createGovUkPayPayment(oldestPayment.paymentId, GovUkPayPaymentStatus.SUCCESS))
            whenever(mockGovUkPayClient.getPayment(newerPayment.paymentId))
                .thenReturn(createGovUkPayPayment(newerPayment.paymentId, GovUkPayPaymentStatus.SUCCESS))

            // Act
            val result = paymentService.findRecoverablePaymentReference(journeyId)

            // Assert
            assertEquals("oldest-reference", result)
            inOrder(mockGovUkPayClient) {
                verify(mockGovUkPayClient).getPayment(oldestPayment.paymentId)
                verify(mockGovUkPayClient).getPayment(newerPayment.paymentId)
            }
            verify(mockGovUkPayClient, never()).getPayment(manualInterventionPayment.paymentId)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockGovUkPayClient, never()).createPayment(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
            verify(mockPaymentRepository, never()).save(any<Payment>())
        }

        @Test
        fun `returns null when GOV UK Pay reports that no INDETERMINATE payment succeeded`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val indeterminatePayment = createPayment(status = PaymentStatus.INDETERMINATE, incompleteProperty = incompleteProperty)
            val createdPayment = createPayment(paymentId = "created-payment-id", incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(indeterminatePayment, createdPayment))
            whenever(mockGovUkPayClient.getPayment(indeterminatePayment.paymentId))
                .thenReturn(createGovUkPayPayment(indeterminatePayment.paymentId, GovUkPayPaymentStatus.CREATED))

            // Act
            val result = paymentService.findRecoverablePaymentReference(journeyId)

            // Assert
            assertNull(result)
            verify(mockGovUkPayClient).getPayment(indeterminatePayment.paymentId)
            verify(mockGovUkPayClient, never()).getPayment(createdPayment.paymentId)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
            verify(mockPaymentRepository, never()).save(any<Payment>())
        }

        @Test
        fun `returns a newer INDETERMINATE payment GOV UK Pay reports succeeded over an older one it reports capturable`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val olderPayment = createStoredPayment(OTHER_PAYMENT_ID, day = 1, incompleteProperty = incompleteProperty)
            val newerPayment = createStoredPayment(PAYMENT_ID, day = 2, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty))
                .thenReturn(listOf(olderPayment, newerPayment))
            stubGovUkPayStatus(OTHER_PAYMENT_ID, GovUkPayPaymentStatus.CAPTURABLE)
            stubGovUkPayStatus(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS)

            // Act
            val result = paymentService.findRecoverablePaymentReference(journeyId)

            // Assert
            assertEquals(newerPayment.reference, result)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        }

        @Test
        fun `propagates a failed GOV UK Pay lookup without updating anything`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)).thenReturn(
                listOf(
                    createStoredPayment(OTHER_PAYMENT_ID, day = 1, incompleteProperty = incompleteProperty),
                    createStoredPayment(PAYMENT_ID, day = 2, incompleteProperty = incompleteProperty),
                ),
            )
            stubGovUkPayStatus(OTHER_PAYMENT_ID, GovUkPayPaymentStatus.FAILED)
            val lookupFailure = GovUkPayException("GOV.UK Pay lookup failed")
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenThrow(lookupFailure)

            // Act
            val exception = assertThrows<GovUkPayException> { paymentService.findRecoverablePaymentReference(journeyId) }

            // Assert
            assertSame(lookupFailure, exception)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        }

        @Test
        fun `returns null without contacting GOV UK Pay when there are no INDETERMINATE payments`() {
            // Arrange
            setUpIncompleteProperty()

            // Act
            val result = paymentService.findRecoverablePaymentReference(journeyId)

            // Assert
            assertNull(result)
            verifyNoInteractions(mockGovUkPayClient)
        }
    }

    @Nested
    inner class GetPropertyRegistrationPaymentStatusTests {
        @Test
        fun `getPropertyRegistrationPaymentStatus returns the GOV UK Pay status of the journey's payment with the given reference`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val payment = createPayment(paymentId = PAYMENT_ID, reference = PAYMENT_REFERENCE, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findByReferenceAndAssociatedIncompleteProperty(PAYMENT_REFERENCE, incompleteProperty))
                .thenReturn(payment)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS))

            // Act
            val paymentStatusCheck = paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE)

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
            assertThrows<IllegalStateException> { paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE) }
            verifyNoInteractions(mockGovUkPayClient, mockPaymentRepository)
        }

        @Test
        fun `getPropertyRegistrationPaymentStatus throws when the journey has no payment with the given reference`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            whenever(mockPaymentRepository.findByReferenceAndAssociatedIncompleteProperty(PAYMENT_REFERENCE, incompleteProperty))
                .thenReturn(null)

            // Act
            val exception =
                assertThrows<IllegalStateException> { paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE) }

            // Assert
            assertContains(exception.message!!, journeyId)
            assertContains(exception.message!!, PAYMENT_REFERENCE)
            verifyNoInteractions(mockGovUkPayClient)
        }

        @Test
        fun `getPropertyRegistrationPaymentStatus records an INDETERMINATE payment that failed, then checks the referenced payment`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val referencedPayment = stubReferencedPayment(incompleteProperty, status = PaymentStatus.CREATED)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)).thenReturn(
                listOf(createStoredPayment(OTHER_PAYMENT_ID, day = 1, incompleteProperty = incompleteProperty), referencedPayment),
            )
            stubGovUkPayStatus(OTHER_PAYMENT_ID, GovUkPayPaymentStatus.FAILED)
            stubGovUkPayStatus(PAYMENT_ID, GovUkPayPaymentStatus.CAPTURABLE)

            // Act
            val paymentStatusCheck = paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE)

            // Assert
            assertEquals(PAYMENT_ID, paymentStatusCheck.paymentId)
            assertEquals(PaymentStatus.CAPTURABLE, paymentStatusCheck.status)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(OTHER_PAYMENT_ID),
                eq(listOf(PaymentStatus.INDETERMINATE)),
                eq(PaymentStatus.FAILED),
                any(),
            )
        }

        @Test
        fun `getPropertyRegistrationPaymentStatus records and returns a referenced INDETERMINATE payment that was cancelled`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val referencedPayment = stubReferencedPayment(incompleteProperty, status = PaymentStatus.INDETERMINATE)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)).thenReturn(listOf(referencedPayment))
            stubGovUkPayStatus(PAYMENT_ID, GovUkPayPaymentStatus.CANCELLED)

            // Act
            val paymentStatusCheck = paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE)

            // Assert
            assertEquals(PAYMENT_ID, paymentStatusCheck.paymentId)
            assertEquals(PaymentStatus.CANCELLED, paymentStatusCheck.status)
            verify(mockGovUkPayClient, times(1)).getPayment(PAYMENT_ID)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
                eq(PAYMENT_ID),
                eq(listOf(PaymentStatus.INDETERMINATE)),
                eq(PaymentStatus.CANCELLED),
                any(),
            )
        }

        @Test
        fun `getPropertyRegistrationPaymentStatus does not check payments that require manual intervention`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val referencedPayment = stubReferencedPayment(incompleteProperty, status = PaymentStatus.CREATED)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)).thenReturn(
                listOf(
                    createStoredPayment(OTHER_PAYMENT_ID, day = 1, status = PaymentStatus.MANUAL_INTERVENTION, incompleteProperty),
                    referencedPayment,
                ),
            )
            stubGovUkPayStatus(PAYMENT_ID, GovUkPayPaymentStatus.CREATED)

            // Act
            val paymentStatusCheck = paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE)

            // Assert
            assertEquals(PAYMENT_ID, paymentStatusCheck.paymentId)
            verify(mockGovUkPayClient, never()).getPayment(OTHER_PAYMENT_ID)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        }

        @ParameterizedTest
        @EnumSource(GovUkPayPaymentStatus::class, names = ["CREATED", "CAPTURABLE"])
        fun `getPropertyRegistrationPaymentStatus returns an INDETERMINATE payment still in progress instead of the referenced one`(
            govUkPayStatus: GovUkPayPaymentStatus,
        ) {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val referencedPayment = stubReferencedPayment(incompleteProperty, status = PaymentStatus.CREATED)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)).thenReturn(
                listOf(createStoredPayment(OTHER_PAYMENT_ID, day = 1, incompleteProperty = incompleteProperty), referencedPayment),
            )
            stubGovUkPayStatus(OTHER_PAYMENT_ID, govUkPayStatus)

            // Act
            val paymentStatusCheck = paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE)

            // Assert
            assertEquals(OTHER_PAYMENT_ID, paymentStatusCheck.paymentId)
            assertEquals(PaymentStatus.fromGovUKPayStatus(govUkPayStatus), paymentStatusCheck.status)
            verify(mockGovUkPayClient, never()).getPayment(PAYMENT_ID)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        }

        @Test
        fun `getPropertyRegistrationPaymentStatus returns the oldest INDETERMINATE payment GOV UK Pay reports in progress`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val referencedPayment = stubReferencedPayment(incompleteProperty, status = PaymentStatus.INDETERMINATE, day = 2)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)).thenReturn(
                listOf(referencedPayment, createStoredPayment(OTHER_PAYMENT_ID, day = 1, incompleteProperty = incompleteProperty)),
            )
            stubGovUkPayStatus(OTHER_PAYMENT_ID, GovUkPayPaymentStatus.SUBMITTED)
            stubGovUkPayStatus(PAYMENT_ID, GovUkPayPaymentStatus.CAPTURABLE)

            // Act
            val paymentStatusCheck = paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE)

            // Assert
            assertEquals(OTHER_PAYMENT_ID, paymentStatusCheck.paymentId)
            assertEquals(PaymentStatus.CREATED, paymentStatusCheck.status)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        }

        @Test
        fun `getPropertyRegistrationPaymentStatus returns an INDETERMINATE payment that succeeded before an older capturable one`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val referencedPayment = stubReferencedPayment(incompleteProperty, status = PaymentStatus.INDETERMINATE, day = 2)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)).thenReturn(
                listOf(createStoredPayment(OTHER_PAYMENT_ID, day = 1, incompleteProperty = incompleteProperty), referencedPayment),
            )
            stubGovUkPayStatus(OTHER_PAYMENT_ID, GovUkPayPaymentStatus.CAPTURABLE)
            stubGovUkPayStatus(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS)

            // Act
            val paymentStatusCheck = paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE)

            // Assert
            assertEquals(PAYMENT_ID, paymentStatusCheck.paymentId)
            assertEquals(PaymentStatus.SUCCEEDED, paymentStatusCheck.status)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        }

        @Test
        fun `getPropertyRegistrationPaymentStatus returns the oldest INDETERMINATE payment that succeeded and flags the others`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val referencedPayment = stubReferencedPayment(incompleteProperty, status = PaymentStatus.INDETERMINATE, day = 1)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)).thenReturn(
                listOf(createStoredPayment(OTHER_PAYMENT_ID, day = 2, incompleteProperty = incompleteProperty), referencedPayment),
            )
            stubGovUkPayStatus(OTHER_PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS)
            stubGovUkPayStatus(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS)
            stubConditionalUpdate(PaymentStatus.IN_PROGRESS_STATUSES, PaymentStatus.MANUAL_INTERVENTION, OTHER_PAYMENT_ID)

            // Act
            val paymentStatusCheck = paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE)

            // Assert
            assertEquals(PAYMENT_ID, paymentStatusCheck.paymentId)
            assertEquals(PaymentStatus.SUCCEEDED, paymentStatusCheck.status)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(eq(PAYMENT_ID), any(), any(), any())
        }

        @Test
        fun `getPropertyRegistrationPaymentStatus throws when another INDETERMINATE payment that succeeded is already registered`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val referencedPayment = stubReferencedPayment(incompleteProperty, status = PaymentStatus.INDETERMINATE, day = 1)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)).thenReturn(
                listOf(referencedPayment, createStoredPayment(OTHER_PAYMENT_ID, day = 2, incompleteProperty = incompleteProperty)),
            )
            stubGovUkPayStatus(OTHER_PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS)
            stubGovUkPayStatus(PAYMENT_ID, GovUkPayPaymentStatus.SUCCESS)
            whenever(mockPaymentRepository.findStatusByPaymentId(OTHER_PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)

            // Act, Assert
            assertThrows<IllegalStateException> { paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE) }
        }

        @Test
        fun `getPropertyRegistrationPaymentStatus updates nothing when checking an INDETERMINATE payment fails`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val referencedPayment = stubReferencedPayment(incompleteProperty, status = PaymentStatus.INDETERMINATE, day = 2)
            whenever(mockPaymentRepository.findAllByAssociatedIncompleteProperty(incompleteProperty)).thenReturn(
                listOf(createStoredPayment(OTHER_PAYMENT_ID, day = 1, incompleteProperty = incompleteProperty), referencedPayment),
            )
            stubGovUkPayStatus(OTHER_PAYMENT_ID, GovUkPayPaymentStatus.FAILED)
            whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenThrow(GovUkPayException("GOV.UK Pay lookup failed"))

            // Act, Assert
            assertThrows<GovUkPayException> { paymentService.getPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE) }
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        }

        private fun stubReferencedPayment(
            incompleteProperty: LandlordIncompleteProperty,
            status: PaymentStatus,
            day: Int = 2,
        ): Payment {
            val payment = createStoredPayment(PAYMENT_ID, day, status, incompleteProperty, PAYMENT_REFERENCE)
            whenever(mockPaymentRepository.findByReferenceAndAssociatedIncompleteProperty(PAYMENT_REFERENCE, incompleteProperty))
                .thenReturn(payment)
            return payment
        }
    }

    @Nested
    inner class GetLatestSucceededPaymentForPropertyTests {
        @Test
        fun `getLatestSucceededPaymentForProperty returns the latest succeeded payment associated with the property`() {
            // Arrange
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            val payment = createPayment(paymentId = PAYMENT_ID, status = PaymentStatus.SUCCEEDED)
            whenever(
                mockPaymentRepository.findFirstByAssociatedPropertyAndStatusOrderByPaymentCreatedAtDesc(
                    propertyOwnership,
                    PaymentStatus.SUCCEEDED,
                ),
            ).thenReturn(payment)

            // Act
            val latestPayment = paymentService.getLatestSucceededPaymentForProperty(propertyOwnership)

            // Assert
            assertSame(payment, latestPayment)
        }

        @Test
        fun `getLatestSucceededPaymentForProperty returns null when the property has no succeeded payment`() {
            // Arrange
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            whenever(
                mockPaymentRepository.findFirstByAssociatedPropertyAndStatusOrderByPaymentCreatedAtDesc(
                    propertyOwnership,
                    PaymentStatus.SUCCEEDED,
                ),
            ).thenReturn(null)

            // Act
            val latestPayment = paymentService.getLatestSucceededPaymentForProperty(propertyOwnership)

            // Assert
            assertNull(latestPayment)
        }
    }

    private fun createPaymentService(
        annualFeeInPence: Int = 2000,
        gratisPeriodEndDate: String = "2027-11-14",
        paymentStatusRetryDelay: Duration = Duration.ZERO,
    ) = PaymentService(
        annualFeeInPence,
        gratisPeriodEndDate,
        paymentStatusRetryDelay,
        MANUAL_INTERVENTION_EMAIL_ADDRESS,
        mockGovUkPayClient,
        mockPaymentRepository,
        mockLandlordIncompletePropertiesRepository,
        mockSavedJourneyStateRepository,
        mockUserToLandlordService,
        mockPropertyRegistrationService,
        mockAbsoluteUrlProvider,
        messageSource,
        TransactionTemplate(mockTransactionManager),
        mockEntityManager,
        mockManualInterventionEmailSender,
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
        stubPaymentReturnUrl()
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

    private fun stubPaymentReturnUrl() {
        whenever(mockAbsoluteUrlProvider.buildPropertyRegistrationPaymentReturnUri(eq(journeyId), any())).thenReturn(URI(returnUrl))
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
        paymentId: String = PAYMENT_ID,
    ) {
        whenever(
            mockPaymentRepository.updateStatusIfCurrentStatusIn(eq(paymentId), eq(expectedStatuses), eq(newStatus), any()),
        ).thenReturn(1)
    }

    private fun stubStoredPayment(payment: Payment = createPayment(paymentId = PAYMENT_ID)) {
        whenever(mockPaymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment))
    }

    private fun stubTransaction(): TransactionStatus {
        val transactionStatus = SimpleTransactionStatus()
        whenever(mockTransactionManager.getTransaction(anyOrNull())).thenReturn(transactionStatus)
        return transactionStatus
    }

    private fun stubFailingCommits(vararg failureMessages: String) {
        // Rolling back discards each attempt's changes to the payment
        whenever(mockPaymentRepository.findById(PAYMENT_ID)).thenAnswer { Optional.of(createPayment(paymentId = PAYMENT_ID)) }
        doThrow(*failureMessages.map { TransactionSystemException(it) }.toTypedArray())
            .doNothing()
            .whenever(mockTransactionManager)
            .commit(any())
    }

    private fun createStoredPayment(
        paymentId: String,
        day: Int,
        status: PaymentStatus = PaymentStatus.INDETERMINATE,
        incompleteProperty: LandlordIncompleteProperty,
        reference: String = "$paymentId-reference",
    ) = createPayment(
        paymentId = paymentId,
        reference = reference,
        paymentCreatedAt = Instant.parse("2025-01-0${day}T00:00:00Z"),
        status = status,
        incompleteProperty = incompleteProperty,
    )

    private fun stubGovUkPayStatus(
        paymentId: String,
        status: GovUkPayPaymentStatus,
    ) {
        whenever(mockGovUkPayClient.getPayment(paymentId)).thenReturn(createGovUkPayPayment(paymentId, status))
    }

    companion object {
        private const val PAYMENT_ID = "payment-id"
        private const val PAYMENT_REFERENCE = "payment-reference"
        private const val OTHER_PAYMENT_ID = "other-payment-id"
        private const val FINISHED_PAYMENT_ID = "finished-payment-id"
        private val PAYMENT_STATUS_RETRY_DELAY: Duration = Duration.ofMillis(100)
        private const val MANUAL_INTERVENTION_EMAIL_ADDRESS = "payments-support@example.com"

        @JvmStatic
        fun provideTransientStatusCheckFailures() =
            listOf(
                Arguments.of(GovUkPayException("Network error", ResourceAccessException("Connection reset"))),
                Arguments.of(GovUkPayException(HttpStatus.INTERNAL_SERVER_ERROR, null, null, RuntimeException())),
                Arguments.of(GovUkPayException(HttpStatus.BAD_GATEWAY, null, null, RuntimeException())),
                Arguments.of(GovUkPayException(HttpStatus.SERVICE_UNAVAILABLE, null, null, RuntimeException())),
            )

        @JvmStatic
        fun provideNonTransientStatusCheckFailures() =
            listOf(
                Arguments.of(GovUkPayException(HttpStatus.BAD_REQUEST, null, null, RuntimeException())),
                Arguments.of(GovUkPayException(HttpStatus.NOT_FOUND, null, null, RuntimeException())),
                Arguments.of(GovUkPayException(HttpStatus.TOO_MANY_REQUESTS, null, null, RuntimeException())),
                Arguments.of(GovUkPayException("No response body")),
                Arguments.of(GovUkPayException("Malformed response", RestClientException("Unreadable response"))),
                Arguments.of(GovUkPayException("Interrupted", InterruptedException())),
            )

        @JvmStatic
        fun provideUnfinishedInProgressPaymentStatuses() =
            listOf(
                Arguments.of(PaymentStatus.CREATED, GovUkPayPaymentStatus.CREATED),
                Arguments.of(PaymentStatus.CREATED, GovUkPayPaymentStatus.STARTED),
                Arguments.of(PaymentStatus.CREATED, GovUkPayPaymentStatus.SUBMITTED),
                Arguments.of(PaymentStatus.CREATED, GovUkPayPaymentStatus.CAPTURABLE),
                Arguments.of(PaymentStatus.INDETERMINATE, GovUkPayPaymentStatus.CAPTURABLE),
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
