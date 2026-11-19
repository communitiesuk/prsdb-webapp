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

@PrsdbScheduledTask("deactivate-missing-detail-properties")
class DeactivateMissingDetailPropertiesTaskApplicationRunner(
    private val context: ApplicationContext,
    private val taskLogic: DeactivateMissingDetailPropertiesTaskLogic,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        println("Executing deactivate missing detail properties task")

        val failureCount = taskLogic.deactivateMissingDetailProperties()

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
    private val activeInactiveService: ActiveInactiveService,
    private val featureFlagManager: FeatureFlagManager,
) {
    fun deactivateMissingDetailProperties(): Int {
        // TODO PDJB-2149: Remove the ACTIVE_INACTIVE check when the flag is removed
        if (!featureFlagManager.checkFeature(ACTIVE_INACTIVE)) {
            println("Active/inactive feature is disabled; no properties were deactivated.")
            return 0
        }

        val failureCount = activeInactiveService.deactivatePropertiesWithOverdueTasksReturningFailureCount()

        println("Missing detail property deactivation task complete. There were $failureCount failures.")
        return failureCount
    }
}
