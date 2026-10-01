package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperty
import uk.gov.communities.prsdb.webapp.database.entity.Payment
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PaymentRepository
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPrsdbUserData.Companion.createPrsdbUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import java.time.Instant
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class PaymentServiceTests {
    @Mock
    private lateinit var mockGovUkPayClient: GovUkPayClient

    @Mock
    private lateinit var mockPaymentRepository: PaymentRepository

    @Mock
    private lateinit var mockTransactionManager: PlatformTransactionManager

    @Mock
    private lateinit var mockRegisterProperty: () -> PropertyOwnership

    private lateinit var paymentService: PaymentService

    @BeforeEach
    fun setUp() {
        paymentService = createPaymentService(annualFeeInPence = 2000)
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
        val paymentServiceWithPolicyFee = createPaymentService(annualFeeInPence)

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
    fun `finalisePayment throws when GOV UK Pay reports the payment is still in progress`() {
        // Act, Assert
        assertThrows<IllegalArgumentException> {
            paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CREATED, mockRegisterProperty)
        }
        verifyNoInteractions(mockPaymentRepository, mockGovUkPayClient, mockRegisterProperty)
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["FAILED", "CANCELLED"])
    fun `finalisePayment sets an unsuccessful status if the payment is in progress and returns the current status`(
        govUkPayStatus: PaymentStatus,
    ) {
        // Arrange
        whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(govUkPayStatus)

        // Act
        val status = paymentService.finalisePayment(PAYMENT_ID, govUkPayStatus, mockRegisterProperty)

        // Assert
        assertEquals(govUkPayStatus, status)
        verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
            eq(PAYMENT_ID),
            eq(PaymentStatus.IN_PROGRESS_STATUSES),
            eq(govUkPayStatus),
            any(),
        )
        verifyNoInteractions(mockGovUkPayClient, mockRegisterProperty)
    }

    @Test
    fun `finalisePayment returns the current status without updating it when GOV UK Pay reports the payment succeeded`() {
        // Arrange
        whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)

        // Act
        val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, mockRegisterProperty)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(any(), any(), any(), any())
        verifyNoInteractions(mockGovUkPayClient, mockRegisterProperty)
    }

    @Test
    fun `finalisePayment throws when the payment does not exist`() {
        // Arrange
        whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(null)

        // Act, Assert
        assertThrows<IllegalStateException> {
            paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.SUCCEEDED, mockRegisterProperty)
        }
    }

    @Test
    fun `finalisePayment returns the current status without side effects when the payment cannot be claimed`() {
        // Arrange
        whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.SUCCEEDED)

        // Act
        val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        verify(mockPaymentRepository).updateStatusIfCurrentStatusIn(
            eq(PAYMENT_ID),
            eq(listOf(PaymentStatus.CREATED)),
            eq(PaymentStatus.CAPTURABLE),
            any(),
        )
        verifyNoInteractions(mockTransactionManager, mockGovUkPayClient, mockRegisterProperty)
    }

    @Test
    fun `finalisePayment registers the property, links it to the payment and captures the payment once claimed`() {
        // Arrange
        val payment = createPayment()
        val property = MockLandlordData.createPropertyOwnership(renewalDate = LocalDate.of(2027, 6, 1))
        stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
        stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
        stubTransaction()
        whenever(mockRegisterProperty.invoke()).thenReturn(property)
        whenever(mockPaymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment))

        // Act
        val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty)

        // Assert
        assertEquals(PaymentStatus.SUCCEEDED, status)
        assertEquals(PaymentStatus.SUCCEEDED, payment.status)
        assertEquals(property, payment.associatedProperty)
        assertNull(payment.associatedIncompleteProperty)
        assertEquals(property.renewalDate, payment.forPeriodEnding)
        val inOrder = inOrder(mockPaymentRepository, mockRegisterProperty, mockGovUkPayClient, mockTransactionManager)
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
        inOrder.verify(mockGovUkPayClient).capturePayment(PAYMENT_ID)
        inOrder.verify(mockTransactionManager).commit(any())
        verify(mockGovUkPayClient, never()).cancelPayment(any())
    }

    @Test
    fun `finalisePayment does not register the property if the payment is no longer capturable when finalising`() {
        // Arrange
        stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
        stubTransaction()
        whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

        // Act
        val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty)

        // Assert
        assertEquals(PaymentStatus.CANCELLED, status)
        verifyNoInteractions(mockRegisterProperty, mockGovUkPayClient)
    }

    @Test
    fun `finalisePayment rolls back, cancels the payment and returns the current status when registration fails`() {
        // Arrange
        stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
        stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
        stubTransaction()
        whenever(mockRegisterProperty.invoke()).thenThrow(EntityExistsException("Address already registered"))
        whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

        // Act
        val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty)

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
        whenever(mockPaymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(createPayment()))
        doThrow(GovUkPayException("Capture failed")).whenever(mockGovUkPayClient).capturePayment(PAYMENT_ID)
        whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

        // Act
        val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty)

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
    fun `finalisePayment rethrows and leaves the payment capturable when cancelling the payment fails`() {
        // Arrange
        val registrationException = EntityExistsException("Address already registered")
        stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
        stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
        stubTransaction()
        whenever(mockRegisterProperty.invoke()).thenThrow(registrationException)
        doThrow(GovUkPayException("Cancel failed")).whenever(mockGovUkPayClient).cancelPayment(PAYMENT_ID)

        // Act
        val exception =
            assertThrows<GovUkPayException> {
                paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty)
            }

        // Assert
        assertEquals(registrationException, exception.suppressed.single())
        verify(mockPaymentRepository, never()).updateStatusIfCurrentStatusIn(
            any(),
            eq(listOf(PaymentStatus.CAPTURABLE)),
            eq(PaymentStatus.CANCELLED),
            any(),
        )
    }

    @Test
    fun `finalisePayment rolls back and cancels the payment without capturing it when registration marks the transaction for rollback`() {
        // Arrange
        stubConditionalUpdate(listOf(PaymentStatus.CREATED), PaymentStatus.CAPTURABLE)
        stubConditionalUpdate(listOf(PaymentStatus.CAPTURABLE), PaymentStatus.SUCCEEDED)
        val transactionStatus = stubTransaction()
        whenever(mockRegisterProperty.invoke()).thenAnswer {
            transactionStatus.setRollbackOnly()
            MockLandlordData.createPropertyOwnership()
        }
        whenever(mockPaymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(createPayment()))
        whenever(mockPaymentRepository.findStatusByPaymentId(PAYMENT_ID)).thenReturn(PaymentStatus.CANCELLED)

        // Act
        val status = paymentService.finalisePayment(PAYMENT_ID, PaymentStatus.CAPTURABLE, mockRegisterProperty)

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

    private fun createPaymentService(annualFeeInPence: Int) =
        PaymentService(annualFeeInPence, mockGovUkPayClient, mockPaymentRepository, TransactionTemplate(mockTransactionManager))

    private fun stubConditionalUpdate(
        expectedStatuses: List<PaymentStatus>,
        newStatus: PaymentStatus,
    ) {
        whenever(
            mockPaymentRepository.updateStatusIfCurrentStatusIn(eq(PAYMENT_ID), eq(expectedStatuses), eq(newStatus), any()),
        ).thenReturn(1)
    }

    private fun stubTransaction(): TransactionStatus {
        val transactionStatus = SimpleTransactionStatus()
        whenever(mockTransactionManager.getTransaction(anyOrNull())).thenReturn(transactionStatus)
        return transactionStatus
    }

    private fun createPayment() =
        Payment(
            paymentId = PAYMENT_ID,
            amountInPence = 1000,
            reference = "reference",
            paymentCreatedAt = Instant.now(),
            forPeriodEnding = LocalDate.of(2026, 1, 1),
            // Deliberately stale, as the managed entity may have been loaded before the payment was claimed
            status = PaymentStatus.CREATED,
            incompleteProperty = LandlordIncompleteProperty(createPrsdbUser(), MockSavedJourneyStateData.createSavedJourneyState()),
        )

    companion object {
        private const val PAYMENT_ID = "payment-id"

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
    }
}
