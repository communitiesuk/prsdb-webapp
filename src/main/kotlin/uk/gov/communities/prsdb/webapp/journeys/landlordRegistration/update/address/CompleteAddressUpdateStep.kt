package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.address

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteAddressUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateAddressJourneyState>() {
    override fun mode(state: UpdateAddressJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateAddressJourneyState) {
        landlordService.updateLandlordAddress(
            SecurityContextHolder.getContext().authentication.name,
            state.getAddress(),
        )
    }

    override fun resolveNextDestination(
        state: UpdateAddressJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteAddressUpdateStep(
    stepConfig: CompleteAddressUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateAddressJourneyState>(stepConfig)
