package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService

@JourneyFrameworkComponent
class CompleteEpcUpdateStepConfig(
    private val propertyComplianceService: PropertyComplianceService,
) : AbstractInternalStepConfig<Complete, UpdateEpcJourneyState>() {
    override fun mode(state: UpdateEpcJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateEpcJourneyState) {
        // TODO PDJB-765 - save updated compliance
    }

    override fun resolveNextDestination(
        state: UpdateEpcJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteEpcUpdateStep(
    stepConfig: CompleteEpcUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateEpcJourneyState>(stepConfig)
