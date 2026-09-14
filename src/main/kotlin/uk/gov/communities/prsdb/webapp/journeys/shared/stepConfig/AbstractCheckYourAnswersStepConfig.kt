package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel

/**
 * Renders a "check your answers" page and validates the submitted data has not
 * changed since page load. Does NOT delete the journey on submission — journeys
 * that should be deleted immediately after their CYA step must extend
 * [AbstractCompleteJourneyStepConfig] instead, or defer deletion to a later step.
 */
abstract class AbstractCheckYourAnswersStepConfig<TState : CheckYourAnswersJourneyState> :
    AbstractRequestableStepConfig<Complete, CheckAnswersFormModel, TState>() {
    override val formModelClass = CheckAnswersFormModel::class

    override fun chooseTemplate(state: TState) = "forms/checkAnswersForm"

    override fun mode(state: TState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun resolvePageContent(
        state: TState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> =
        defaultContent + ("submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()))

    override fun enrichSubmittedDataBeforeValidation(
        state: TState,
        formData: FormData,
    ): FormData {
        checkJourneyNotModifiedSincePageLoad(state, formData)
        return formData
    }

    override fun resolveNextDestination(
        state: TState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    private fun checkJourneyNotModifiedSincePageLoad(
        state: TState,
        formData: FormData,
    ) {
        val submittedData = formData["submittedFilteredJourneyData"] as? String ?: return
        val currentData = CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData())
        if (submittedData != currentData) {
            throw CyaDataHasChangedException("Journey data has changed since the page was loaded")
        }
    }
}

abstract class AbstractCheckYourAnswersStep<TState : CheckYourAnswersJourneyState>(
    stepConfig: AbstractCheckYourAnswersStepConfig<TState>,
) : RequestableStep<Complete, CheckAnswersFormModel, TState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-answers"
    }
}

/**
 * A "check your answers" step whose submission both persists the final answers
 * (via subclass [AbstractCheckYourAnswersStepConfig.afterStepDataIsAdded] overrides)
 * and deletes the journey state immediately — i.e. it represents the true, final
 * completion of the journey, not just answer confirmation.
 */
abstract class AbstractCompleteJourneyStepConfig<TState : CheckYourAnswersJourneyState> : AbstractCheckYourAnswersStepConfig<TState>() {
    override fun resolveNextDestination(
        state: TState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

abstract class AbstractCompleteJourneyStep<TState : CheckYourAnswersJourneyState>(
    stepConfig: AbstractCompleteJourneyStepConfig<TState>,
) : AbstractCheckYourAnswersStep<TState>(stepConfig)
