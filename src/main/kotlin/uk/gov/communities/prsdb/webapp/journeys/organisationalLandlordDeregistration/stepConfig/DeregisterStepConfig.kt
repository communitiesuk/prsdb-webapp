package uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.OrganisationalLandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent("organisationalLandlordDeregistrationDeregisterStepConfig")
class DeregisterStepConfig(
    private val userToLandlordService: UserToLandlordService,
    private val deregistrationEmailSender: EmailNotificationService<OrganisationalLandlordDeregistrationConfirmationEmail>,
) : AbstractInternalStepConfig<Complete, OrganisationalLandlordDeregistrationJourneyState>() {
    override fun mode(state: OrganisationalLandlordDeregistrationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: OrganisationalLandlordDeregistrationJourneyState) {
        // TODO: PDJB-1483 - Delete org landlord logic
        val organisationLandlord = userToLandlordService.getCurrentOrganisationLandlordForUser()
        deregistrationEmailSender.sendEmail(
            organisationLandlord.email,
            OrganisationalLandlordDeregistrationConfirmationEmail(
                registrantName = organisationLandlord.registrantName,
                organisationName = organisationLandlord.name,
            ),
        )
    }
}

@JourneyFrameworkComponent("organisationalLandlordDeregistrationDeregisterStep")
class DeregisterStep(
    stepConfig: DeregisterStepConfig,
) : JourneyStep.InternalStep<Complete, OrganisationalLandlordDeregistrationJourneyState>(stepConfig)
