package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep

interface GasSafetyState : JourneyState {
    val gasSafetyStep: GasSafetyStep
    val gasSafetyIssueDateStep: GasSafetyIssueDateStep
    val gasSafetyEngineerNumberStep: GasSafetyEngineerNumberStep
    val gasSafetyCertificateUploadStep: GasSafetyCertificateUploadStep
    val gasSafetyUploadConfirmationStep: GasSafetyUploadConfirmationStep
    val gasSafetyOutdatedStep: GasSafetyOutdatedStep
    val gasSafetyExemptionStep: GasSafetyExemptionStep
    val gasSafetyExemptionReasonStep: GasSafetyExemptionReasonStep
    val gasSafetyExemptionOtherReasonStep: GasSafetyExemptionOtherReasonStep
    val gasSafetyExemptionConfirmationStep: GasSafetyExemptionConfirmationStep
    val gasSafetyExemptionMissingStep: GasSafetyExemptionMissingStep
}
