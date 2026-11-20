package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_WITHOUT_PROPERTY_DEACTIVATION_PERIOD_IN_MONTHS
import uk.gov.communities.prsdb.webapp.services.LandlordDeactivationService
import kotlin.system.exitProcess

@PrsdbScheduledTask("deactivate-inactive-landlords-scheduled-task")
class DeactivateInactiveLandlordsTaskApplicationRunner(
    private val context: ApplicationContext,
    private val taskLogic: DeactivateInactiveLandlordsTaskLogic,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        println("Executing deactivate inactive landlords scheduled task")

        val failureCount = taskLogic.deactivateInactiveLandlords()

        val exitCode = if (failureCount > 0) 1 else 0
        val code =
            SpringApplication.exit(context, { exitCode }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }
}

// Not @Transactional, so each landlord's deactivation commits on its own and one failure can't roll back the others
@PrsdbTaskService
class DeactivateInactiveLandlordsTaskLogic(
    private val landlordDeactivationService: LandlordDeactivationService,
    private val featureFlagManager: FeatureFlagManager,
) {
    fun deactivateInactiveLandlords(): Int {
        // TODO PDJB-2149: Remove the ACTIVE_INACTIVE check when the flag is removed
        if (!featureFlagManager.checkFeature(ACTIVE_INACTIVE)) {
            println("The active/inactive feature is disabled. No landlords were deactivated.")
            return 0
        }

        val neverRegisteredIds = landlordDeactivationService.getIdsOfLandlordsWhoNeverRegisteredAProperty()
        val noActivePropertiesIds = landlordDeactivationService.getIdsOfLandlordsWithNoActiveProperties()
        println(
            "Found ${neverRegisteredIds.size} landlords who have not registered a property within " +
                "$LANDLORD_WITHOUT_PROPERTY_DEACTIVATION_PERIOD_IN_MONTHS months and ${noActivePropertiesIds.size} landlords " +
                "with no active properties.",
        )

        var deactivatedCount = 0
        var failureCount = 0
        (neverRegisteredIds + noActivePropertiesIds).distinct().forEach { landlordId ->
            try {
                if (landlordDeactivationService.deactivateLandlordIfNoActiveProperties(landlordId)) {
                    println("Deactivated landlord with id: $landlordId")
                    deactivatedCount++
                }
            } catch (ex: Exception) {
                println("Failed to deactivate landlord with id: $landlordId")
                println("Exception message: ${ex.message}")
                println("Stack trace: ${ex.stackTraceToString()}")
                failureCount++
            }
        }

        println("Deactivate inactive landlords task complete. Landlords deactivated: $deactivatedCount. Failures: $failureCount.")
        return failureCount
    }
}
