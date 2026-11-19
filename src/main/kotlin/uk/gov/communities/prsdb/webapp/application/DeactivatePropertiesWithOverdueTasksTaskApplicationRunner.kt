package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.services.ActiveInactiveService
import kotlin.system.exitProcess

@PrsdbScheduledTask("deactivate-properties-with-overdue-tasks-scheduled-task")
class DeactivatePropertiesWithOverdueTasksTaskApplicationRunner(
    private val context: ApplicationContext,
    private val taskLogic: DeactivatePropertiesWithOverdueTasksTaskLogic,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        println("Executing deactivate properties with overdue tasks scheduled task")

        val failureCount = taskLogic.deactivatePropertiesWithOverdueTasks()

        val exitCode = if (failureCount > 0) 1 else 0
        val code =
            SpringApplication.exit(context, { exitCode }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }
}

@PrsdbTaskService
class DeactivatePropertiesWithOverdueTasksTaskLogic(
    private val activeInactiveService: ActiveInactiveService,
    private val featureFlagManager: FeatureFlagManager,
) {
    fun deactivatePropertiesWithOverdueTasks(): Int {
        // TODO PDJB-2149: Remove the ACTIVE_INACTIVE check when the flag is removed
        if (!featureFlagManager.checkFeature(ACTIVE_INACTIVE)) {
            println("Active/inactive feature is disabled; no properties were deactivated.")
            return 0
        }

        val failureCount = activeInactiveService.deactivatePropertiesWithOverdueTasksReturningFailureCount()

        println("Overdue property deactivation task complete. There were $failureCount failures.")
        return failureCount
    }
}
