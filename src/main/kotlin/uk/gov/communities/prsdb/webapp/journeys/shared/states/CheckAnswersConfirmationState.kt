package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep

interface CheckAnswersConfirmationState : CheckYourAnswersJourneyState {
    override val cyaStep: AbstractCheckYourAnswersStep<*>

    val answersChangedSinceConfirmation: Boolean
        get() = cyaStep.formModelOrNull?.answersLastChangedAt.orEmpty() != answersLastChanged?.toString().orEmpty()
}
