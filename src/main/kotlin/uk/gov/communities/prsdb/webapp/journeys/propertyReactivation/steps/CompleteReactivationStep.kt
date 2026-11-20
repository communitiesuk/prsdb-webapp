package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import jakarta.persistence.EntityExistsException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.PropertyReactivationService

@JourneyFrameworkComponent
class CompleteReactivationStepConfig(
    private val propertyReactivationService: PropertyReactivationService,
) : AbstractInternalStepConfig<Complete, PropertyReactivationJourneyState>() {
    override fun mode(state: PropertyReactivationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: PropertyReactivationJourneyState) {
        try {
            propertyReactivationService.reactivatePropertyIfAddressAvailable(state.propertyOwnershipId, state.uprn)
        } catch (_: EntityExistsException) {
            state.isPropertyAlreadyRegistered = true
        }
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
