package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.application.ReconcilePaymentsTaskApplicationRunner
import uk.gov.communities.prsdb.webapp.services.PaymentReconciliationService

@ExtendWith(MockitoExtension::class)
class ReconcilePaymentsTaskApplicationRunnerTests {
    @Mock
    private lateinit var mockContext: ApplicationContext

    @Mock
    private lateinit var mockPaymentReconciliationService: PaymentReconciliationService

    @InjectMocks
    private lateinit var runner: ReconcilePaymentsTaskApplicationRunner

    @Test
    fun `reconcilePaymentsAndGetExitCode returns 0 when no payments fail to reconcile`() {
        // Arrange
        whenever(mockPaymentReconciliationService.reconcilePayments(any())).thenReturn(0)

        // Act
        val exitCode = runner.reconcilePaymentsAndGetExitCode()

        // Assert
        assertEquals(0, exitCode)
    }

    @Test
    fun `reconcilePaymentsAndGetExitCode returns 1 when payments fail to reconcile`() {
        // Arrange
        whenever(mockPaymentReconciliationService.reconcilePayments(any())).thenReturn(2)

        // Act
        val exitCode = runner.reconcilePaymentsAndGetExitCode()

        // Assert
        assertEquals(1, exitCode)
    }
}
