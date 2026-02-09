package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep

interface GasSafetyState : JourneyState {
    // TODO PDJB-467 - Implement these steps and add to the GasSafetyTask
    // val gasSafetyStep: GasSafetyStep
    // val gasSafetyIssueDateStep: GasSafetyIssueDateStep
    val gasSafetyEngineerNumberStep: GasSafetyEngineerNumberStep
    val gasSafetyCertificateUploadStep: GasSafetyCertificateUploadStep
    val gasSafetyUploadConfirmationStep: GasSafetyUploadConfirmationStep
    // val gasSafetyOutdatedStep: GasSafetyOutdatedStep
    // val gasSafetyExemptionStep: GasSafetyExemptionStep
    // val gasSafetyExemptionReasonStep: GasSafetyExemptionReasonStep
    // val gasSafetyExemptionOtherReasonStep: GasSafetyExemptionOtherReasonStep
    // val gasSafetyExemptionConfirmationStep: GasSafetyExemptionConfirmationStep
    // val gasSafetyExemptionMissingStep: GasSafetyExemptionMissingStep
}
