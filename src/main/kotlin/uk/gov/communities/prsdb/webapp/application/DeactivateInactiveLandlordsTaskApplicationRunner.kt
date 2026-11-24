package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_WITHOUT_PROPERTY_DEACTIVATION_PERIOD_IN_MONTHS
import uk.gov.communities.prsdb.webapp.services.DeactivationService
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

@PrsdbTaskService
class DeactivateInactiveLandlordsTaskLogic(
    private val deactivationService: DeactivationService,
) {
    fun deactivateInactiveLandlords(): Int {
        val neverRegisteredIds = deactivationService.getIdsOfLandlordsWhoNeverRegisteredAProperty()
        val noActivePropertiesIds = deactivationService.getIdsOfLandlordsWithNoActiveProperties()
        println(
            "Found ${neverRegisteredIds.size} landlords who have not registered a property within " +
                "$LANDLORD_WITHOUT_PROPERTY_DEACTIVATION_PERIOD_IN_MONTHS months and ${noActivePropertiesIds.size} landlords " +
                "with no active properties.",
        )

        var deactivatedCount = 0
        var failureCount = 0
        (neverRegisteredIds + noActivePropertiesIds).distinct().forEach { landlordId ->
            try {
                if (deactivationService.deactivateLandlordIfNoActiveProperties(landlordId)) {
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
