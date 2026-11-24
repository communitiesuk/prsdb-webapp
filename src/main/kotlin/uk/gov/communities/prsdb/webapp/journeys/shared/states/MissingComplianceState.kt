package uk.gov.communities.prsdb.webapp.journeys.shared.states

interface MissingComplianceState : CheckAnswersConfirmationState {
    val isMissingComplianceCheckApplicable: Boolean
    val isGasCertMissing: Boolean
    val isElectricalCertMissing: Boolean
    val isEpcMissing: Boolean
}
