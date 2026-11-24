package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep

interface MissingComplianceState : CheckYourAnswersJourneyState {
    override val cyaStep: AbstractCheckYourAnswersStep<*>

    val isMissingComplianceCheckApplicable: Boolean
    val isGasCertMissing: Boolean
    val isElectricalCertMissing: Boolean
    val isEpcMissing: Boolean

    val answersChangedSinceConfirmation: Boolean
        get() = cyaStep.formModelOrNull?.answersLastChangedAt.orEmpty() != answersLastChanged?.toString().orEmpty()
}
