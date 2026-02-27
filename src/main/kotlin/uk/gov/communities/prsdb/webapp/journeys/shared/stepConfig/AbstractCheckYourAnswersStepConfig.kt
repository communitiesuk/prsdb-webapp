package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel

abstract class AbstractCheckYourAnswersStepConfig2<T : Enum<T>, TState : CheckYourAnswersJourneyState<T>> :
    AbstractRequestableStepConfig<Complete, CheckAnswersFormModel, TState>() {
    override val formModelClass = CheckAnswersFormModel::class

    override fun chooseTemplate(state: TState) = "forms/checkAnswersForm"

    override fun mode(state: TState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun enrichSubmittedDataBeforeValidation(
        state: TState,
        formData: PageData,
    ): PageData = formData + (CheckAnswersFormModel::storedJourneyData.name to state.getSubmittedStepData())

    override fun resolveNextDestination(
        state: TState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

abstract class AbstractCheckYourAnswersStep<T : Enum<T>, TState : CheckYourAnswersJourneyState<T>>(
    stepConfig: AbstractCheckYourAnswersStepConfig2<T, TState>,
) : RequestableStep<Complete, CheckAnswersFormModel, TState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-answers"
    }
}
