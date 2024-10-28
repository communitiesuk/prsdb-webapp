package uk.gov.communities.prsdb.webapp.multipageforms

data class Step<TStepId : StepId>(
    val page: Page,
    val persistAfterSubmit: Boolean = false,
    val nextStep: (Map<String, Any>) -> StepAction<TStepId>,
    val isSatisfied: (Map<String, Any>) -> Boolean = { journeyData ->
        page.isSatisfied(journeyData)
    },
)

sealed class StepAction<TStepId : StepId> {
    data class GoToStep<TStepId : StepId>(
        val stepId: TStepId,
    ) : StepAction<TStepId>()

    data class Redirect<TStepId : StepId>(
        val path: String,
    ) : StepAction<TStepId>()
}

class StepBuilder<TStepId : StepId> {
    private var page: Page? = null
    private var nextStep: ((Map<String, Any>) -> StepAction<TStepId>)? = null

    fun page(init: PageBuilder.() -> Unit) {
        page = PageBuilder().apply(init).build()
    }

    fun goToStep(stepId: TStepId) {
        nextStep = { StepAction.GoToStep(stepId) }
    }

    fun redirect(path: String) {
        nextStep = { StepAction.Redirect(path) }
    }

    fun build() = Step(page = page!!, nextStep = nextStep!!)
}
