package uk.gov.communities.prsdb.webapp.application

import jakarta.transaction.Transactional
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationDeletionService
import kotlin.system.exitProcess

@PrsdbScheduledTask("jl-invitation-deletion-scheduled-task")
class DeleteExpiredJointLandlordInvitationsTaskApplicationRunner(
    private val context: ApplicationContext,
    private val taskLogic: DeleteExpiredJointLandlordInvitationsTaskLogic,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        println("Executing joint landlord invitation retention cleanup scheduled task")

        taskLogic.deleteJointLandlordInvitationsPastRetentionPeriod()

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }
}

@PrsdbTaskService
class DeleteExpiredJointLandlordInvitationsTaskLogic(
    private val jointLandlordInvitationDeletionService: JointLandlordInvitationDeletionService,
) {
    @Transactional
    fun deleteJointLandlordInvitationsPastRetentionPeriod() {
        val deletedIds = jointLandlordInvitationDeletionService.deleteInvitationsPastRetentionPeriod()

        deletedIds.forEach { id ->
            println("Deleted joint landlord invitation past retention period with id: $id")
        }

        println("Deleted ${deletedIds.size} joint landlord invitations past the retention period.")
    }
}
