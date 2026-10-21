package uk.gov.communities.prsdb.webapp.application

import jakarta.transaction.Transactional
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationDeletionService
import kotlin.system.exitProcess

@PrsdbScheduledTask("org-ll-invitation-deletion-scheduled-task")
class DeleteExpiredOrganisationalLandlordInvitationsTaskApplicationRunner(
    private val context: ApplicationContext,
    private val taskLogic: DeleteExpiredOrganisationalLandlordInvitationsTaskLogic,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        println("Executing organisational landlord invitation retention cleanup scheduled task")

        taskLogic.deleteOrganisationalLandlordInvitationsPastRetentionPeriod()

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }
}

@PrsdbTaskService
class DeleteExpiredOrganisationalLandlordInvitationsTaskLogic(
    private val organisationalLandlordInvitationDeletionService: OrganisationalLandlordInvitationDeletionService,
) {
    @Transactional
    fun deleteOrganisationalLandlordInvitationsPastRetentionPeriod() {
        val deletedIds = organisationalLandlordInvitationDeletionService.deleteInvitationsPastRetentionPeriod()

        deletedIds.forEach { id ->
            println("Deleted organisational landlord invitation past retention period with id: $id")
        }

        println("Deleted ${deletedIds.size} organisational landlord invitations past the retention period.")
    }
}
