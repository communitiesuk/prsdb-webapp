package uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.OrganisationalLandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
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
) : AbstractInternalStepConfig<Complete, OrganisationalLandlordDeregistrationJourneyState>() {
    override fun mode(state: OrganisationalLandlordDeregistrationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: OrganisationalLandlordDeregistrationJourneyState) {
        val orgLandlord = userToLandlordService.getCurrentOrganisationLandlordForUser()

        val jointlyOwnedProperties = orgLandlord.landlordships.filterNot { it.isSolelyOwnedBy(orgLandlord) }

        landlordDeregistrationService.deregisterOrganisationalLandlord(orgLandlord)

        // TODO: PDJB-1485 - Send org landlord deregistration confirmation emails to all team members

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
