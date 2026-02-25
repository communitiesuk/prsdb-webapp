package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState2
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel

abstract class AbstractCheckYourAnswersStepConfig<TState : CheckYourAnswersJourneyState> :
    AbstractRequestableStepConfig<Complete, CheckAnswersFormModel, TState>() {
    override val formModelClass = CheckAnswersFormModel::class

    override fun chooseTemplate(state: TState) = "forms/checkAnswersForm"

    override fun mode(state: TState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    protected lateinit var childJourneyId: String

    override fun afterStepIsReached(state: TState) {
        if (state.cyaChildJourneyIdIfInitialized == null) {
            state.initialiseCyaChildJourney()
        }

        childJourneyId = state.cyaChildJourneyIdIfInitialized
            ?: throw UnrecoverableJourneyStateException(state.journeyId, "CYA child journey ID should be initialised")
    }

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

abstract class AbstractCheckYourAnswersStep<TState : CheckYourAnswersJourneyState>(
    stepConfig: AbstractCheckYourAnswersStepConfig<TState>,
) : RequestableStep<Complete, CheckAnswersFormModel, TState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-answers"
    }
}

@Suppress("ktlint:standard:max-line-length")
abstract class AbstractCheckYourAnswersStepConfig2<TCheckableElements : Enum<TCheckableElements>, TState : CheckYourAnswersJourneyState2<TCheckableElements>> : AbstractRequestableStepConfig<Complete, CheckAnswersFormModel, TState>() {
    override val formModelClass = CheckAnswersFormModel::class

    override fun chooseTemplate(state: TState) = "forms/checkAnswersForm"

    override fun mode(state: TState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    protected fun childJourneyId(element: TCheckableElements): String = element.name

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
