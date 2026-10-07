package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import jakarta.persistence.EntityExistsException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyHelper
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class SavePropertyRegistrationDataStepConfig(
    private val propertyRegistrationJourneyHelper: PropertyRegistrationJourneyHelper,
) : AbstractInternalStepConfig<Complete, PropertyRegistrationJourneyState>() {
    override fun mode(state: PropertyRegistrationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: PropertyRegistrationJourneyState) {
        try {
            propertyRegistrationJourneyHelper.registerProperty(state)
        } catch (_: EntityExistsException) {
            state.propertyDetailsTask.addressTask.isAddressAlreadyRegistered = true
            return
        }
    }

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination =
        if (state.propertyDetailsTask.addressTask.isAddressAlreadyRegistered == true) {
            Destination(state.propertyDetailsTask.addressTask.alreadyRegisteredStep)
        } else {
            state.deleteJourney()
            defaultDestination
        }
}

@JourneyFrameworkComponent
class SavePropertyRegistrationDataStep(
    stepConfig: SavePropertyRegistrationDataStepConfig,
) : JourneyStep.InternalStep<Complete, PropertyRegistrationJourneyState>(stepConfig)
