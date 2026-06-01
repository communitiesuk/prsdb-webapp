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

        jointLandlordInvitationExpiryService.expirePendingInvitations()

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }
}
