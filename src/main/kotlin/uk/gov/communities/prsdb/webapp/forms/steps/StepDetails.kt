package uk.gov.communities.prsdb.webapp.forms.steps

data class StepDetails<T : StepId>(
    val step: Step<T>?,
    val subPageNumber: Int?,
)
