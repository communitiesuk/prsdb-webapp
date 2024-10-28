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

class StepBuilder<TStepId : StepId> {
    private var page: Page? = null
    private var nextStep: ((Map<String, Any>) -> StepAction)? = null

    fun page(init: PageBuilder.() -> Unit) {
        page = PageBuilder().apply(init).build()
    }

    fun goToStep(stepId: TStepId) {
        nextStep = { StepAction.GoToStep(stepId) }
    }

    fun redirect(path: String) {
        nextStep = { StepAction.Redirect(path) }
    }

    fun build() = Step<TStepId>(page = page!!, nextStep = nextStep!!)
}
