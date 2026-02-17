package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CompleteOwnershipTypeUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractInternalStepConfig<Complete, UpdateOwnershipTypeJourneyState>() {
    override fun mode(state: UpdateOwnershipTypeJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOwnershipTypeJourneyState) {
        propertyOwnershipService.updateOwnershipType(
            state.propertyId,
            state.ownershipTypeStep.formModel.notNullValue(OwnershipTypeFormModel::ownershipType),
        )
    }

    override fun resolveNextDestination(
        state: UpdateOwnershipTypeJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteOwnershipTypeUpdateStep(
    stepConfig: CompleteOwnershipTypeUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateOwnershipTypeJourneyState>(stepConfig)
