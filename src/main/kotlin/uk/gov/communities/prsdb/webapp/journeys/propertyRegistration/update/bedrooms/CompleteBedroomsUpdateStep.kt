package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.bedrooms

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CompleteBedroomsUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractInternalStepConfig<Complete, UpdateBedroomsJourneyState>() {
    override fun mode(state: UpdateBedroomsJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateBedroomsJourneyState) {
        propertyOwnershipService.updateBedrooms(
            id = state.propertyId,
            numberOfBedrooms = state.bedrooms.formModel.notNullValue(NumberOfBedroomsFormModel::numberOfBedrooms).toInt(),
            initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
        )
    }

    override fun resolveNextDestination(
        state: UpdateBedroomsJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteBedroomsUpdateStep(
    stepConfig: CompleteBedroomsUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateBedroomsJourneyState>(stepConfig)
