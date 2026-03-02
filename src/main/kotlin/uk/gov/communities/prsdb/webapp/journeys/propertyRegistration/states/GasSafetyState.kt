package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasSafetyExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasSafetyMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideGasSafetyLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveGasSafetyUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasSafetyStep

interface GasSafetyState : JourneyState {
    val hasGasSupplyStep: HasGasSupplyStep
    val hasGasSafetyStep: HasGasSafetyStep
    val gasSafetyIssueDateStep: GasSafetyIssueDateStep
    val uploadGasSafetyStep: UploadGasSafetyStep
    val checkGasSafetyUploadsStep: CheckGasSafetyUploadsStep
    val removeGasSafetyUploadStep: RemoveGasSafetyUploadStep
    val gasSafetyExpiredStep: GasSafetyExpiredStep
    val gasSafetyMissingStep: GasSafetyMissingStep
    val provideGasSafetyLaterStep: ProvideGasSafetyLaterStep
    val checkGasSafetyAnswersStep: CheckGasSafetyAnswersStep
}
