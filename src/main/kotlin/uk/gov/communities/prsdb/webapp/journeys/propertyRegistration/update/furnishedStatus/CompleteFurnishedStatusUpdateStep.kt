package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.furnishedStatus

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CompleteFurnishedStatusUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractInternalStepConfig<Complete, UpdateFurnishedStatusJourneyState>() {
    override fun mode(state: UpdateFurnishedStatusJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateFurnishedStatusJourneyState) {
        propertyOwnershipService.updateFurnishedStatus(
            id = state.propertyId,
            furnishedStatus = state.furnishedStatus.formModel.notNullValue(FurnishedStatusFormModel::furnishedStatus),
            initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
        )
    }

    override fun resolveNextDestination(
        state: UpdateFurnishedStatusJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteFurnishedStatusUpdateStep(
    stepConfig: CompleteFurnishedStatusUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateFurnishedStatusJourneyState>(stepConfig)
