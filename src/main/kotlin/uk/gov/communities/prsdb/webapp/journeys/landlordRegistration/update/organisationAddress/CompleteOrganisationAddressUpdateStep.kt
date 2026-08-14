package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationAddress

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteOrganisationAddressUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateOrganisationAddressJourneyState>() {
    override fun mode(state: UpdateOrganisationAddressJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOrganisationAddressJourneyState) {
        landlordService.updateOrganisationLandlordAddress(state.addressTask.getAddress())
    }

    override fun resolveNextDestination(
        state: UpdateOrganisationAddressJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteOrganisationAddressUpdateStep(
    stepConfig: CompleteOrganisationAddressUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateOrganisationAddressJourneyState>(stepConfig)
