package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteGoverningBodyUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateGoverningBodyJourneyState>() {
    override fun mode(state: UpdateGoverningBodyJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateGoverningBodyJourneyState) {
        val members =
            state.orgGovBodyMembersTask.governingBodyMembersMap
                ?.toSortedMap()
                ?.values
                ?.toList()
                ?: throw PrsdbWebException("Governing body member state is missing")

        landlordService.updateOrganisationLandlordGoverningBodyMembers(members)
    }

    override fun resolveNextDestination(
        state: UpdateGoverningBodyJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteGoverningBodyUpdateStep(
    stepConfig: CompleteGoverningBodyUpdateStepConfig,
) : InternalStep<Complete, UpdateGoverningBodyJourneyState>(stepConfig)
