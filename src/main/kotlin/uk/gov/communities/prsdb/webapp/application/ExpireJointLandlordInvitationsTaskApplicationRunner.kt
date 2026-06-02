package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationExpiryService
import kotlin.system.exitProcess

@PrsdbScheduledTask("expire-joint-landlord-invitations-scheduled-task")
class ExpireJointLandlordInvitationsTaskApplicationRunner(
    private val context: ApplicationContext,
    private val jointLandlordInvitationExpiryService: JointLandlordInvitationExpiryService,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("Executing expire joint landlord invitations scheduled task")

        // Separating into its own method to allow this to be tested without "exitProcess" being called
        expireJointLandlordInvitationsTaskLogic()

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }

    private fun expireJointLandlordInvitationsTaskLogic() {
        val expiredIds = jointLandlordInvitationExpiryService.expirePendingInvitations()

        expiredIds.forEach { id ->
            println("Expired joint landlord invitation with id: $id")
        }

        println("Expired ${expiredIds.size} joint landlord invitations.")
    }

    companion object {
        const val EXPIRE_JOINT_LANDLORD_INVITATIONS_TASK_METHOD_NAME = "expireJointLandlordInvitationsTaskLogic"
    }
}
