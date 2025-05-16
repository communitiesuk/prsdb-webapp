package uk.gov.communities.prsdb.webapp.forms.steps

interface StepId {
    val urlPathSegment: String
}

interface GroupedStepId<T : Enum<T>> : StepId {
    val groupIdentifier: T
}

interface UpdateStepId<T : Enum<T>> : GroupedStepId<T> {
    val isCheckYourAnswersStepId: Boolean
}
