package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.clients.GovUkPayClient
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentFailureType
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.models.dataModels.PaymentStatusCheckDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPayment
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentState
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import java.time.Instant
import java.time.LocalDate

class PaymentServiceTests {
    private val mockGovUkPayClient: GovUkPayClient = mock()
    private val paymentService = PaymentService(govUkPayClient = mockGovUkPayClient, annualFeeInPence = 2000)

    @ParameterizedTest
    @MethodSource("provideWorkedExamples")
    fun `calculateProRatedFeeInPence matches external worked examples`(
        annualFeeInPence: Int,
        today: LocalDate,
        renewalDate: LocalDate,
        expectedFeeInPence: Int,
    ) {
        // Arrange
        val paymentServiceWithPolicyFee = PaymentService(govUkPayClient = mockGovUkPayClient, annualFeeInPence = annualFeeInPence)

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

    @ParameterizedTest
    @MethodSource("provideGovUkPayStatusesAndExpectedStatuses")
    fun `getPaymentStatus maps the GovUkPay payment status to a payment status`(
        govUkPayStatus: GovUkPayPaymentStatus,
        expectedStatus: PaymentStatus,
    ) {
        // Arrange
        whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(govUkPayStatus))

        // Act
        val paymentStatusCheck = paymentService.getPaymentStatus(PAYMENT_ID)

        // Assert
        assertEquals(PAYMENT_ID, paymentStatusCheck.paymentId)
        assertEquals(expectedStatus, paymentStatusCheck.status)
    }

    @ParameterizedTest
    @MethodSource("provideFailedGovUkPayStatusesCodesAndExpectedFailureTypes")
    fun `getPaymentStatus maps the GovUkPay error code to a failure type for failed or cancelled payments`(
        govUkPayStatus: GovUkPayPaymentStatus,
        govUkPayCode: String?,
        expectedStatus: PaymentStatus,
        expectedFailureType: PaymentFailureType,
    ) {
        // Arrange
        whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(govUkPayStatus, govUkPayCode))

        // Act
        val paymentStatusCheck = paymentService.getPaymentStatus(PAYMENT_ID)

        // Assert
        assertEquals(PaymentStatusCheckDataModel(PAYMENT_ID, expectedStatus, expectedFailureType), paymentStatusCheck)
    }

    @ParameterizedTest
    @EnumSource(GovUkPayPaymentStatus::class, names = ["CREATED", "STARTED", "SUBMITTED", "CAPTURABLE", "SUCCESS"])
    fun `getPaymentStatus does not set a failure type for payments that have not failed or been cancelled`(
        govUkPayStatus: GovUkPayPaymentStatus,
    ) {
        // Arrange
        whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenReturn(createGovUkPayPayment(govUkPayStatus, "P0010"))

        // Act
        val paymentStatusCheck = paymentService.getPaymentStatus(PAYMENT_ID)

        // Assert
        assertNull(paymentStatusCheck.failureType)
    }

    @Test
    fun `getPaymentStatus propagates GovUkPayException from the client`() {
        // Arrange
        val govUkPayException = GovUkPayException("GovUkPay request failed: connection refused")
        whenever(mockGovUkPayClient.getPayment(PAYMENT_ID)).thenThrow(govUkPayException)

        // Act
        val thrownException = assertThrows<GovUkPayException> { paymentService.getPaymentStatus(PAYMENT_ID) }

        // Assert
        assertSame(govUkPayException, thrownException)
    }

    private fun createGovUkPayPayment(
        status: GovUkPayPaymentStatus,
        code: String? = null,
    ) = GovUkPayPayment(
        paymentId = PAYMENT_ID,
        amount = 2000,
        reference = "reference",
        description = "description",
        createdDate = Instant.parse("2026-10-01T09:00:00Z"),
        state = GovUkPayPaymentState(status = status, finished = status in FINISHED_GOV_UK_PAY_STATUSES, code = code),
    )

    companion object {
        private const val PAYMENT_ID = "payment-id"

        private val FINISHED_GOV_UK_PAY_STATUSES =
            setOf(
                GovUkPayPaymentStatus.SUCCESS,
                GovUkPayPaymentStatus.FAILED,
                GovUkPayPaymentStatus.CANCELLED,
                GovUkPayPaymentStatus.ERROR,
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
                Arguments.of(GovUkPayPaymentStatus.CANCELLED, "P0030", PaymentStatus.CANCELLED, PaymentFailureType.CANCELLED_BY_USER),
                Arguments.of(GovUkPayPaymentStatus.CANCELLED, "P0040", PaymentStatus.CANCELLED, PaymentFailureType.CANCELLED_BY_SERVICE),
                Arguments.of(GovUkPayPaymentStatus.ERROR, "P0050", PaymentStatus.FAILED, PaymentFailureType.PAYMENT_PROVIDER_ERROR),
                Arguments.of(GovUkPayPaymentStatus.FAILED, "P9999", PaymentStatus.FAILED, PaymentFailureType.UNKNOWN),
                Arguments.of(GovUkPayPaymentStatus.CANCELLED, null, PaymentStatus.CANCELLED, PaymentFailureType.UNKNOWN),
            )
    }
}
