package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.services.IncompletePropertiesService
import kotlin.system.exitProcess

@PrsdbScheduledTask("delete-incomplete-properties-scheduled-task")
class DeleteIncompletePropertiesTaskApplicationRunner(
    private val context: ApplicationContext,
    private val incompletePropertiesService: IncompletePropertiesService,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        println("Executing delete incomplete properties scheduled task")

        // Separating into its own method to allow this to be tested without "exitProcess" being called
        deleteIncompletePropertiesTaskLogic()

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }

    private fun deleteIncompletePropertiesTaskLogic() {
        val numberOfRecordsDeleted = incompletePropertiesService.deleteIncompletePropertiesOlderThan28Days()
        println("Deleted $numberOfRecordsDeleted incomplete properties.")
    }

    companion object {
        const val DELETE_INCOMPLETE_PROPERTIES_TASK_METHOD_NAME = "deleteIncompletePropertiesTaskLogic"
    }
}
