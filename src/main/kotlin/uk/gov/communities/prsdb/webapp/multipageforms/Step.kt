package uk.gov.communities.prsdb.webapp.multipageforms

data class Step<TStepId : StepId>(
    val page: Page,
    val persistAfterSubmit: Boolean = false,
    val nextStep: (Map<String, Any>) -> StepAction,
    val isSatisfied: (Map<String, Any>) -> Boolean = { journeyData ->
        page.isSatisfied(journeyData)
    },
)

sealed class StepAction {
    data class GoToStep(
        val stepId: StepId,
    ) : StepAction()

    data class Redirect(
        val path: String,
    ) : StepAction()
}
