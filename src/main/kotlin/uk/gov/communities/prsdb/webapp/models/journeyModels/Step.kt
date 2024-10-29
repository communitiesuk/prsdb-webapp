package uk.gov.communities.prsdb.webapp.models.journeyModels

open class Step<TStepId : StepId>(
    val page: Page,
    val nextStep: (Map<String, Any>) -> StepId,
    val updateContext: ((Map<String, Any>), (Map<String, String>)) -> Map<String, String>,
) {
    fun isSatisfied(formContext: Map<String, Any>): Boolean {
        val pageSubmission = getSubmissionFromFormContext(formContext)
        return page.validateSubmission(pageSubmission)
    }

    fun getSubmissionFromFormContext(formContext: Map<String, Any>): Map<String, String> {
        // TODO add logic here - it will not always be this simple
        return formContext as Map<String, String>
    }
}

class StepBuilder<TStepId : StepId> {
    var page: Page? = null
    var nextStep: ((Map<String, Any>) -> StepId)? = null
    var updateContext: (((Map<String, Any>), (Map<String, String>)) -> Map<String, String>)? = null

    fun build(): Step<TStepId> = Step(page = page!!, nextStep = nextStep!!, updateContext = updateContext!!)
}
