package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep

interface GasSafetyState : JourneyState {
    val uploadGasSafetyStep: GasSafetyCertificateUploadStep
}
