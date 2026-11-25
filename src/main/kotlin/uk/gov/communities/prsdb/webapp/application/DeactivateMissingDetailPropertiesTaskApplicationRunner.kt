package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.services.DeactivationService
import kotlin.system.exitProcess

@PrsdbScheduledTask("deactivate-missing-detail-properties-scheduled-task")
class DeactivateMissingDetailPropertiesTaskApplicationRunner(
    private val context: ApplicationContext,
    private val taskLogic: DeactivateMissingDetailPropertiesTaskLogic,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        println("Executing deactivate missing detail properties task")

        val failureCount = taskLogic.deactivateMissingDetailPropertiesReturningFailureCount()

        val exitCode = if (failureCount > 0) 1 else 0
        val code =
            SpringApplication.exit(context, { exitCode }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }
}

@PrsdbTaskService
class DeactivateMissingDetailPropertiesTaskLogic(
    private val deactivationService: DeactivationService,
) {
    fun deactivateMissingDetailPropertiesReturningFailureCount(): Int {
        val failureCount = deactivationService.deactivateMissingDetailPropertiesPastDueDateReturningFailureCount()

        println("Missing detail property deactivation task complete. There were $failureCount failures.")
        return failureCount
    }
}
