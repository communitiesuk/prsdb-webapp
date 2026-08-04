package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.name

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteNameUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateNameJourneyState>() {
    override fun mode(state: UpdateNameJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateNameJourneyState) {
        val newName = state.nameStep.formModel.notNullValue(NameFormModel::name)
        landlordService.updateLandlordName(newName)
    }

    override fun resolveNextDestination(
        state: UpdateNameJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteNameUpdateStep(
    stepConfig: CompleteNameUpdateStepConfig,
) : InternalStep<Complete, UpdateNameJourneyState>(stepConfig)
