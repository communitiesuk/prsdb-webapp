package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus

class PaymentStatusCheckDataModelTests {
    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["CREATED"])
    fun `isCreated returns true for created status`(status: PaymentStatus) {
        // Arrange
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, status, isCancellable = false)

        // Act
        val result = paymentStatusCheck.isCreated()

        // Assert
        assertTrue(result)
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["CREATED"], mode = EnumSource.Mode.EXCLUDE)
    fun `isCreated returns false for non-created statuses`(status: PaymentStatus) {
        // Arrange
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, status, isCancellable = false)

        // Act
        val result = paymentStatusCheck.isCreated()

        // Assert
        assertFalse(result)
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["CAPTURABLE"])
    fun `isCapturable returns true for capturable status`(status: PaymentStatus) {
        // Arrange
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, status, isCancellable = false)

        // Act
        val result = paymentStatusCheck.isCapturable()

        // Assert
        assertTrue(result)
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["CAPTURABLE"], mode = EnumSource.Mode.EXCLUDE)
    fun `isCapturable returns false for non-capturable statuses`(status: PaymentStatus) {
        // Arrange
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, status, isCancellable = false)

        // Act
        val result = paymentStatusCheck.isCapturable()

        // Assert
        assertFalse(result)
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["CREATED", "CAPTURABLE"])
    fun `isInProgress returns true for in-progress statuses`(status: PaymentStatus) {
        // Arrange
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, status, isCancellable = false)

        // Act
        val result = paymentStatusCheck.isInProgress()

        // Assert
        assertTrue(result)
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["CREATED", "CAPTURABLE"], mode = EnumSource.Mode.EXCLUDE)
    fun `isInProgress returns false for non-in-progress statuses`(status: PaymentStatus) {
        // Arrange
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, status, isCancellable = false)

        // Act
        val result = paymentStatusCheck.isInProgress()

        // Assert
        assertFalse(result)
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["SUCCEEDED"])
    fun `isSucceeded returns true for succeeded status`(status: PaymentStatus) {
        // Arrange
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, status, isCancellable = false)

        // Act
        val result = paymentStatusCheck.isSucceeded()

        // Assert
        assertTrue(result)
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["SUCCEEDED"], mode = EnumSource.Mode.EXCLUDE)
    fun `isSucceeded returns false for non-succeeded statuses`(status: PaymentStatus) {
        // Arrange
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, status, isCancellable = false)

        // Act
        val result = paymentStatusCheck.isSucceeded()

        // Assert
        assertFalse(result)
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["FAILED", "CANCELLED"])
    fun `isFailedOrCancelled returns true for failed or cancelled statuses`(status: PaymentStatus) {
        // Arrange
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, status, isCancellable = false)

        // Act
        val result = paymentStatusCheck.isFailedOrCancelled()

        // Assert
        assertTrue(result)
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus::class, names = ["FAILED", "CANCELLED"], mode = EnumSource.Mode.EXCLUDE)
    fun `isFailedOrCancelled returns false for non-failed or cancelled statuses`(status: PaymentStatus) {
        // Arrange
        val paymentStatusCheck = PaymentStatusCheckDataModel(PAYMENT_ID, status, isCancellable = false)

        // Act
        val result = paymentStatusCheck.isFailedOrCancelled()

        // Assert
        assertFalse(result)
    }

    companion object {
        private const val PAYMENT_ID = "payment-id"
    }
}
