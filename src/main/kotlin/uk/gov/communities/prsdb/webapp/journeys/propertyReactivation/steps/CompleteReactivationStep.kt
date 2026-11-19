package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PropertyReactivationService

@JourneyFrameworkComponent
class CompleteReactivationStepConfig(
    private val addressAvailabilityService: AddressAvailabilityService,
    private val propertyReactivationService: PropertyReactivationService,
) : AbstractInternalStepConfig<Complete, PropertyReactivationJourneyState>() {
    override fun mode(state: PropertyReactivationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: PropertyReactivationJourneyState) {
        if (state.uprn?.let { addressAvailabilityService.isAddressOwned(it) } == true) {
            state.isPropertyAlreadyRegistered = true
            return
        }
        propertyReactivationService.reactivateProperty(state.propertyOwnershipId)
    }

    override fun resolveNextDestination(
        state: PropertyReactivationJourneyState,
        defaultDestination: Destination,
    ): Destination =
        if (state.isPropertyAlreadyRegistered == true) {
            Destination(state.alreadyRegisteredStep)
        } else {
            state.deleteJourney()
            defaultDestination
        }
}

@JourneyFrameworkComponent
class CompleteReactivationStep(
    stepConfig: CompleteReactivationStepConfig,
) : JourneyStep.InternalStep<Complete, PropertyReactivationJourneyState>(stepConfig)
