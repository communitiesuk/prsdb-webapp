package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideGasCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveGasCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep

interface GasSafetyState : JourneyState {
    val isOccupied: Boolean?
    val hasGasSupplyStep: HasGasSupplyStep
    val hasGasCertStep: HasGasCertStep
    val gasCertIssueDateStep: GasCertIssueDateStep
    val uploadGasCertStep: UploadGasCertStep
    val checkGasCertUploadsStep: CheckGasCertUploadsStep
    val removeGasCertUploadStep: RemoveGasCertUploadStep
    val gasCertExpiredStep: GasCertExpiredStep
    val gasCertMissingStep: GasCertMissingStep
    val provideGasCertLaterStep: ProvideGasCertLaterStep
    val checkGasSafetyAnswersStep: CheckGasSafetyAnswersStep
}
