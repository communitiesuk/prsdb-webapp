package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel

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
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }

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
