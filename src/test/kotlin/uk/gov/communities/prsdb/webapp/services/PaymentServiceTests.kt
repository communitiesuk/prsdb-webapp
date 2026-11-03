package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityManager
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
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
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.exceptions.PaymentQuoteExpiredException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentCreationResult
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentQuote
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayCreatedPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockGovUkPayData.Companion.createGovUkPayPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPaymentData.Companion.createPayment
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyRegistrationData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.URI
import java.sql.SQLException
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
    private lateinit var mockPropertyRegistrationService: PropertyRegistrationService

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockSavedJourneyStateRepository: SavedJourneyStateRepository

    @Mock
    private lateinit var mockPaymentReconciliationService: PaymentReconciliationService

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
            verify(mockPaymentRepository).saveAndFlush(paymentCaptor.capture())
            assertEquals(expectedRenewalDate, paymentCaptor.firstValue.forPeriodEnding)
            assertEquals(quote.amountInPence, paymentCaptor.firstValue.amountInPence)
            assertEquals(quote.quoteDate, paymentCaptor.firstValue.quoteDate)
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
            verify(mockPaymentRepository).saveAndFlush(paymentCaptor.capture())
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
            verifyNoInteractions(mockGovUkPayClient, mockPaymentRepository)
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
            verify(mockPaymentRepository, never()).saveAndFlush(any<Payment>())
        }

        @Test
        fun `createPropertyRegistrationPayment marks the draft's CREATED payment for cancellation before creating a new one`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val createdPayment = createPayment(paymentId = OTHER_PAYMENT_ID, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findByAssociatedIncompletePropertyAndStatus(incompleteProperty, PaymentStatus.CREATED))
                .thenReturn(createdPayment)
            whenever(mockPaymentReconciliationService.markForCancellation(OTHER_PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLING)
            stubGovUkPayCreatePayment()

            // Act
            paymentService.createPropertyRegistrationPayment(
                journeyId,
                email,
                paymentService.getPropertyRegistrationPaymentQuote(),
            )

            // Assert
            val inOrder = inOrder(mockPaymentReconciliationService, mockGovUkPayClient)
            inOrder.verify(mockPaymentReconciliationService).markForCancellation(OTHER_PAYMENT_ID)
            inOrder.verify(mockGovUkPayClient).createPayment(any())
        }

        @Test
        fun `createPropertyRegistrationPayment does not create another payment when a payment succeeds before it can be marked`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            val incompleteProperty = setUpIncompletePropertyAndLandlord()
            val createdPayment = createPayment(paymentId = OTHER_PAYMENT_ID, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findByAssociatedIncompletePropertyAndStatus(incompleteProperty, PaymentStatus.CREATED))
                .thenReturn(createdPayment)
            whenever(mockPaymentReconciliationService.markForCancellation(OTHER_PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)

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
            assertContains(exception.message!!, OTHER_PAYMENT_ID)
            verifyNoInteractions(mockGovUkPayClient)
        }

        @Test
        fun `createPropertyRegistrationPayment cancels the new GOV UK Pay payment when another payment is started at the same time`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            setUpIncompletePropertyAndLandlord()
            stubGovUkPayCreatePayment(paymentId = "new-payment-id")
            whenever(mockPaymentRepository.saveAndFlush(any<Payment>()))
                .thenThrow(constraintViolation("uc_payment_one_created_per_incomplete_property"))

            // Act
            val result =
                paymentService.createPropertyRegistrationPayment(
                    journeyId,
                    email,
                    paymentService.getPropertyRegistrationPaymentQuote(),
                )

            // Assert
            assertEquals(PropertyRegistrationPaymentCreationResult.ConcurrentPaymentStarted, result)
            verify(mockGovUkPayClient).cancelPayment("new-payment-id")
        }

        @Test
        fun `createPropertyRegistrationPayment reports the concurrent payment even when cancelling the new GOV UK Pay payment fails`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            setUpIncompletePropertyAndLandlord()
            stubGovUkPayCreatePayment(paymentId = "new-payment-id")
            whenever(mockPaymentRepository.saveAndFlush(any<Payment>()))
                .thenThrow(constraintViolation("uc_payment_one_created_per_incomplete_property"))
            doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment("new-payment-id")

            // Act
            val result =
                paymentService.createPropertyRegistrationPayment(
                    journeyId,
                    email,
                    paymentService.getPropertyRegistrationPaymentQuote(),
                )

            // Assert
            assertEquals(PropertyRegistrationPaymentCreationResult.ConcurrentPaymentStarted, result)
        }

        @Test
        fun `createPropertyRegistrationPayment rethrows a violation of a different constraint`() {
            // Arrange
            paymentService = createPaymentService(gratisPeriodEndDate = pastGratisPeriodEndDate)
            setUpIncompletePropertyAndLandlord()
            stubGovUkPayCreatePayment()
            val violation = constraintViolation("some_other_constraint")
            whenever(mockPaymentRepository.saveAndFlush(any<Payment>())).thenThrow(violation)

            // Act
            val exception =
                assertThrows<DataIntegrityViolationException> {
                    paymentService.createPropertyRegistrationPayment(
                        journeyId,
                        email,
                        paymentService.getPropertyRegistrationPaymentQuote(),
                    )
                }

            // Assert
            assertSame(violation, exception)
            verify(mockGovUkPayClient, never()).cancelPayment(any())
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
                PaymentStatusCheckDataModel(PAYMENT_ID, expectedStatus, failureType = expectedFailureType),
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
                paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CREATED, registrationData)
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
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIs(
                eq(PAYMENT_ID),
                eq(PaymentStatus.CREATED),
                eq(govUkPayStatus),
                any(),
            )
            verifyNoInteractions(mockGovUkPayClient, mockPropertyRegistrationService, mockSavedJourneyStateRepository)
        }

        @Test
        fun `finalisePayment returns the current status without updating it when GOV UK Pay reports the payment succeeded`() {
            // Arrange
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, registrationData)

            // Assert
            assertEquals(PaymentStatus.SUCCEEDED, status)
            verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIs(any(), any(), any(), any())
            verifyNoInteractions(mockGovUkPayClient, mockPropertyRegistrationService, mockSavedJourneyStateRepository)
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

        @ParameterizedTest
        @EnumSource(PaymentStatus::class, names = ["SUCCEEDED", "CANCELLING", "CANCELLED"])
        fun `finalisePayment returns the stored status without side effects when the payment cannot be claimed`(
            storedStatus: PaymentStatus,
        ) {
            // Arrange
            stubTransaction()
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(storedStatus)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(storedStatus, status)
            verify(mockPaymentRepository).updateStatusIfCurrentStatusIs(
                eq(PAYMENT_ID),
                eq(PaymentStatus.CREATED),
                eq(PaymentStatus.SUCCEEDED),
                any(),
            )
            verifyNoInteractions(
                mockPropertyRegistrationService,
                mockSavedJourneyStateRepository,
                mockGovUkPayClient,
                mockPaymentReconciliationService,
            )
        }

        @Test
        fun `finalisePayment registers the property, links it to the payment and captures the payment once claimed`() {
            // Arrange
            // Deliberately stale, as the managed entity may have been loaded before the payment was claimed
            val payment = createPayment(status = PaymentStatus.CREATED)
            val incompleteProperty = checkNotNull(payment.associatedIncompleteProperty)
            val originalPeriodEnd = payment.forPeriodEnding
            val property = MockLandlordData.createPropertyOwnership(renewalDate = originalPeriodEnd.plusDays(1))
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.SUCCEEDED)
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
            inOrder.verify(mockPaymentRepository).updateStatusIfCurrentStatusIs(
                eq(PAYMENT_ID),
                eq(PaymentStatus.CREATED),
                eq(PaymentStatus.SUCCEEDED),
                any(),
            )
            inOrder.verify(mockEntityManager).refresh(payment)
            inOrder.verify(mockPropertyRegistrationService).registerProperty(registrationData.copy(renewalDate = originalPeriodEnd))
            inOrder.verify(mockPaymentRepository).saveAndFlush(payment)
            inOrder.verify(mockEntityManager).detach(incompleteProperty)
            inOrder.verify(mockSavedJourneyStateRepository).delete(incompleteProperty.savedJourneyState)
            inOrder.verify(mockPaymentRepository).flush()
            inOrder.verify(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            inOrder.verify(mockTransactionManager).commit(any())
            verifyNoInteractions(mockPaymentReconciliationService)
        }

        @Test
        fun `finalisePayment rolls back, then marks the payment for cancellation and returns its status when registration fails`() {
            // Arrange
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenThrow(EntityExistsException("Address already registered"))
            whenever(mockPaymentReconciliationService.markForCancellation(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager, never()).commit(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            // Marking waits for the claim's row lock, so it must come after the rollback releases it
            val inOrder = inOrder(mockTransactionManager, mockPaymentReconciliationService)
            inOrder.verify(mockTransactionManager).rollback(any())
            inOrder.verify(mockPaymentReconciliationService).markForCancellation(PAYMENT_ID)
        }

        @Test
        fun `finalisePayment logs why finalising failed when it marks the payment for cancellation`() {
            // Arrange
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenThrow(EntityExistsException("Address already registered"))
            whenever(mockPaymentReconciliationService.markForCancellation(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)
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
        fun `finalisePayment rolls back, then marks the payment for cancellation and returns its status when capture fails`() {
            // Arrange
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.SUCCEEDED)
            stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            stubStoredPayment()
            doThrow(GovUkPayException("Capture failed")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
            whenever(mockPaymentReconciliationService.markForCancellation(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLING)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLING, status)
            verify(mockTransactionManager, never()).commit(any())
            val inOrder = inOrder(mockTransactionManager, mockPaymentReconciliationService)
            inOrder.verify(mockTransactionManager).rollback(any())
            inOrder.verify(mockPaymentReconciliationService).markForCancellation(PAYMENT_ID)
        }

        @Test
        fun `finalisePayment marks the payment for cancellation without capturing when registration marks the transaction for rollback`() {
            // Arrange
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.SUCCEEDED)
            val transactionStatus = stubTransaction()
            whenever(mockPropertyRegistrationService.registerProperty(any())).thenAnswer {
                transactionStatus.setRollbackOnly()
                MockLandlordData.createPropertyOwnership()
            }
            stubStoredPayment()
            whenever(mockPaymentReconciliationService.markForCancellation(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockPaymentReconciliationService).markForCancellation(PAYMENT_ID)
        }

        @Test
        fun `finalisePayment marks the payment for cancellation without capturing it when deleting the journey fails`() {
            // Arrange
            stubConditionalUpdate(PaymentStatus.CREATED, PaymentStatus.SUCCEEDED)
            stubTransaction()
            stubStoredPayment()
            whenever(mockPropertyRegistrationService.registerProperty(any()))
                .thenReturn(MockLandlordData.createPropertyOwnership())
            doThrow(IllegalStateException("Journey could not be deleted")).whenever(mockSavedJourneyStateRepository).delete(any())
            whenever(mockPaymentReconciliationService.markForCancellation(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

            // Act
            val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, registrationData)

            // Assert
            assertEquals(PaymentStatus.CANCELLED, status)
            verify(mockTransactionManager).rollback(any())
            verify(mockGovUkPayClient, never()).capturePayment(any())
            verify(mockPaymentReconciliationService).markForCancellation(PAYMENT_ID)
        }
    }

    @Nested
    inner class GetStoredPropertyRegistrationPaymentStatusTests {
        @Test
        fun `getStoredPropertyRegistrationPaymentStatus returns the stored status without asking GOV UK Pay`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            val payment = createPayment(paymentId = PAYMENT_ID, reference = PAYMENT_REFERENCE, incompleteProperty = incompleteProperty)
            whenever(mockPaymentRepository.findByReferenceAndAssociatedIncompleteProperty(PAYMENT_REFERENCE, incompleteProperty))
                .thenReturn(payment)
            whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CREATED)

            // Act
            val paymentStatusCheck = paymentService.getStoredPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE)

            // Assert
            assertEquals(PaymentStatusCheckDataModel(PAYMENT_ID, PaymentStatus.CREATED), paymentStatusCheck)
            verifyNoInteractions(mockGovUkPayClient)
        }

        @Test
        fun `getStoredPropertyRegistrationPaymentStatus throws when there is no incomplete property for the journey`() {
            // Arrange
            setMockPrincipal(userId)
            whenever(mockLandlordIncompletePropertiesRepository.findBySavedJourneyState_JourneyIdAndUser_Id(journeyId, userId))
                .thenReturn(null)

            // Act, Assert
            assertThrows<IllegalStateException> {
                paymentService.getStoredPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE)
            }
            verifyNoInteractions(mockGovUkPayClient, mockPaymentRepository)
        }

        @Test
        fun `getStoredPropertyRegistrationPaymentStatus throws when the journey has no payment with the given reference`() {
            // Arrange
            val incompleteProperty = setUpIncompleteProperty()
            whenever(mockPaymentRepository.findByReferenceAndAssociatedIncompleteProperty(PAYMENT_REFERENCE, incompleteProperty))
                .thenReturn(null)

            // Act
            val exception =
                assertThrows<IllegalStateException> {
                    paymentService.getStoredPropertyRegistrationPaymentStatus(journeyId, PAYMENT_REFERENCE)
                }

            // Assert
            assertContains(exception.message!!, journeyId)
            assertContains(exception.message!!, PAYMENT_REFERENCE)
            verifyNoInteractions(mockGovUkPayClient)
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
    ) = PaymentService(
        annualFeeInPence,
        gratisPeriodEndDate,
        mockGovUkPayClient,
        mockPaymentRepository,
        mockLandlordIncompletePropertiesRepository,
        mockSavedJourneyStateRepository,
        mockUserToLandlordService,
        mockPropertyRegistrationService,
        mockPaymentReconciliationService,
        mockAbsoluteUrlProvider,
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
        expectedStatus: PaymentStatus,
        newStatus: PaymentStatus,
    ) {
        whenever(
            mockPaymentRepository.updateStatusIfCurrentStatusIs(eq(PAYMENT_ID), eq(expectedStatus), eq(newStatus), any()),
        ).thenReturn(1)
    }

    private fun stubStoredPayment(payment: Payment = createPayment(paymentId = PAYMENT_ID)) {
        whenever(mockPaymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment))
    }

    private fun constraintViolation(constraintName: String) =
        DataIntegrityViolationException(
            "could not execute statement",
            ConstraintViolationException("duplicate key value violates unique constraint", SQLException(), constraintName),
        )

    private fun stubTransaction(): TransactionStatus {
        val transactionStatus = SimpleTransactionStatus()
        whenever(mockTransactionManager.getTransaction(anyOrNull())).thenReturn(transactionStatus)
        return transactionStatus
    }

    companion object {
        private const val PAYMENT_ID = "payment-id"
        private const val PAYMENT_REFERENCE = "payment-reference"
        private const val OTHER_PAYMENT_ID = "other-payment-id"

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
