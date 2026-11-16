package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.services.PaymentReconciliationService
import java.time.Instant
import kotlin.system.exitProcess

@PrsdbScheduledTask("reconcile-payments-scheduled-task")
class ReconcilePaymentsTaskApplicationRunner(
    private val context: ApplicationContext,
    private val taskLogic: ReconcilePaymentsTaskLogic,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        println("Executing reconcile payments scheduled task")

        val failureCount = taskLogic.reconcilePayments()

        val exitCode = if (failureCount > 0) 1 else 0
        val code =
            SpringApplication.exit(context, { exitCode }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }
}

// Not @Transactional, so that each payment's status change commits before GOV.UK Pay is called
@PrsdbTaskService
class ReconcilePaymentsTaskLogic(
    private val paymentReconciliationService: PaymentReconciliationService,
) {
    fun reconcilePayments(): Int = paymentReconciliationService.reconcilePayments(Instant.now())
}
