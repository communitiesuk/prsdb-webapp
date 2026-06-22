package uk.gov.communities.prsdb.webapp.application

import jakarta.transaction.Transactional
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.services.IncompletePropertiesService
import kotlin.system.exitProcess

@PrsdbScheduledTask("delete-incomplete-properties-scheduled-task")
class DeleteIncompletePropertiesTaskApplicationRunner(
    private val context: ApplicationContext,
    private val taskLogic: DeleteIncompletePropertiesTaskLogic,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        println("Executing delete incomplete properties scheduled task")

        taskLogic.deleteIncompleteProperties()

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }
}

@PrsdbTaskService
class DeleteIncompletePropertiesTaskLogic(
    private val incompletePropertiesService: IncompletePropertiesService,
) {
    @Transactional
    fun deleteIncompleteProperties() {
        val numberOfRecordsDeleted = incompletePropertiesService.deleteIncompletePropertiesOlderThan28Days()
        println("Deleted $numberOfRecordsDeleted incomplete properties.")
    }
}
