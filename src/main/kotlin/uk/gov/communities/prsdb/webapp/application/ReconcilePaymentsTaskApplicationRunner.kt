package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.services.PaymentReconciliationService
import java.time.Instant
import kotlin.system.exitProcess

@PrsdbScheduledTask("reconcile-payments-scheduled-task")
class ReconcilePaymentsTaskApplicationRunner(
    private val context: ApplicationContext,
    private val paymentReconciliationService: PaymentReconciliationService,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        exitProcess(reconcilePaymentsAndGetExitCode())
    }

    // Separate from run so that tests can check the exit code without exitProcess stopping the test JVM
    internal fun reconcilePaymentsAndGetExitCode(): Int {
        println("Executing reconcile payments scheduled task")

        val failureCount = paymentReconciliationService.reconcilePayments(Instant.now())

        val exitCode = if (failureCount > 0) 1 else 0
        return SpringApplication.exit(context, { exitCode }).also {
            println("Scheduled task executed. Application will exit now.")
        }
    }
}
