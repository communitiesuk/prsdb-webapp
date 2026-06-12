package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationDeletionService
import kotlin.system.exitProcess

@PrsdbScheduledTask("jl-invitation-deletion-scheduled-task")
class DeleteExpiredJointLandlordInvitationsTaskApplicationRunner(
    private val context: ApplicationContext,
    private val jointLandlordInvitationDeletionService: JointLandlordInvitationDeletionService,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("Executing delete expired joint landlord invitations scheduled task")

        // Separating into its own method to allow this to be tested without "exitProcess" being called
        deleteExpiredJointLandlordInvitationsTaskLogic()

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }

    private fun deleteExpiredJointLandlordInvitationsTaskLogic() {
        val deletedIds = jointLandlordInvitationDeletionService.deleteExpiredInvitations()

        deletedIds.forEach { id ->
            println("Deleted expired joint landlord invitation with id: $id")
        }

        println("Deleted ${deletedIds.size} expired joint landlord invitations.")
    }

    companion object {
        const val DELETE_EXPIRED_JL_INVITATIONS_TASK_METHOD_NAME = "deleteExpiredJointLandlordInvitationsTaskLogic"
    }
}
