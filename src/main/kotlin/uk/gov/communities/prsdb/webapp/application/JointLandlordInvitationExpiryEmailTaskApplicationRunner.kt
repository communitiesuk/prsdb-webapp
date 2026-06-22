package uk.gov.communities.prsdb.webapp.application

import jakarta.transaction.Transactional
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationExpiryEmailService
import kotlin.system.exitProcess

@PrsdbScheduledTask("jl-invitation-expiry-email-scheduled-task")
class JointLandlordInvitationExpiryEmailTaskApplicationRunner(
    private val context: ApplicationContext,
    private val taskLogic: JointLandlordInvitationExpiryEmailTaskLogic,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("Executing joint landlord invitation expiry email scheduled task")

        taskLogic.sendJointLandlordInvitationExpiryEmails()

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }
}

@PrsdbTaskService
class JointLandlordInvitationExpiryEmailTaskLogic(
    private val jointLandlordInvitationExpiryEmailService: JointLandlordInvitationExpiryEmailService,
) {
    @Transactional
    fun sendJointLandlordInvitationExpiryEmails() {
        val processedIds = jointLandlordInvitationExpiryEmailService.sendExpiryEmailsForExpiredInvitations()

        processedIds.forEach { id ->
            println("Sent expiry email for joint landlord invitation with id: $id")
        }

        println("Sent expiry emails for ${processedIds.size} joint landlord invitations.")
    }
}
