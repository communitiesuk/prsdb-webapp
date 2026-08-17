package uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.OrganisationalLandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.SwapToIndividualNudgeEmailService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent("organisationalLandlordDeregistrationDeregisterStepConfig")
class DeregisterStepConfig(
    private val landlordDeregistrationService: LandlordDeregistrationService,
    private val userToLandlordService: UserToLandlordService,
    private val securityContextService: SecurityContextService,
    private val swapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService,
    private val deregistrationEmailSender: EmailNotificationService<OrganisationalLandlordDeregistrationConfirmationEmail>,
) : AbstractInternalStepConfig<Complete, OrganisationalLandlordDeregistrationJourneyState>() {
    override fun mode(state: OrganisationalLandlordDeregistrationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: OrganisationalLandlordDeregistrationJourneyState) {
        val orgLandlord = userToLandlordService.getCurrentOrganisationLandlordForUser()

        // The name is read before deletion, as the organisation is unavailable afterwards
        val organisationName = orgLandlord.name

        val jointlyOwnedProperties = orgLandlord.landlordships.filterNot { it.isSolelyOwnedBy(orgLandlord) }

        landlordDeregistrationService.deregisterOrganisationalLandlord(orgLandlord)

        // Stored only once the deletion has succeeded, as this doubles as the "deregistered in this session" flag
        landlordDeregistrationService.addDeregisteredOrganisationNameToSession(organisationName)

        deregistrationEmailSender.sendEmail(
            orgLandlord.registrantEmail,
            OrganisationalLandlordDeregistrationConfirmationEmail(
                registrantName = orgLandlord.registrantName,
                organisationName = organisationName,
            ),
        )

        jointlyOwnedProperties.forEach {
            swapToIndividualNudgeEmailService.sendNudgeEmailIfApplicable(it)
        }

        securityContextService.refreshContext()
    }

    override fun resolveNextDestination(
        state: OrganisationalLandlordDeregistrationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent("organisationalLandlordDeregistrationDeregisterStep")
class DeregisterStep(
    stepConfig: DeregisterStepConfig,
) : JourneyStep.InternalStep<Complete, OrganisationalLandlordDeregistrationJourneyState>(stepConfig)
