package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.application.ReconcilePaymentsTaskLogic
import uk.gov.communities.prsdb.webapp.services.PaymentReconciliationService

@ExtendWith(MockitoExtension::class)
class ReconcilePaymentsTaskApplicationRunnerTests {
    @Mock
    private lateinit var paymentReconciliationService: PaymentReconciliationService

    @InjectMocks
    private lateinit var taskLogic: ReconcilePaymentsTaskLogic

    @Test
    fun `reconcilePayments returns zero when no payments fail to reconcile`() {
        // Arrange
        whenever(paymentReconciliationService.reconcilePayments(any())).thenReturn(0)

        // Act
        val failureCount = taskLogic.reconcilePayments()

        // Assert
        assertEquals(0, failureCount)
    }

    @Test
    fun `reconcilePayments returns the number of payments that failed to reconcile`() {
        // Arrange
        whenever(paymentReconciliationService.reconcilePayments(any())).thenReturn(2)

        // Act
        val failureCount = taskLogic.reconcilePayments()

        // Assert
        assertEquals(2, failureCount)
    }
}
