package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveElectricalCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep

interface ElectricalSafetyState : JourneyState {
    val hasElectricalCertStep: HasElectricalCertStep
    val electricalCertExpiryDateStep: ElectricalCertExpiryDateStep
    val uploadElectricalCertStep: UploadElectricalCertStep
    val checkElectricalCertUploadsStep: CheckElectricalCertUploadsStep
    val removeElectricalCertUploadStep: RemoveElectricalCertUploadStep
    val electricalCertExpiredStep: ElectricalCertExpiredStep
    val electricalCertMissingStep: ElectricalCertMissingStep
    val provideElectricalCertLaterStep: ProvideElectricalCertLaterStep
    val checkElectricalSafetyAnswersStep: CheckElectricalSafetyAnswersStep
}
