package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep

interface GasSafetyState : JourneyState {
    val gasSafetyCertificateUploadStep: GasSafetyCertificateUploadStep
    val gasSafetyUploadConfirmationStep: GasSafetyUploadConfirmationStep
}
