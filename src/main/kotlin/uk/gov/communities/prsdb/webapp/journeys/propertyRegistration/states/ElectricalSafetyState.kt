package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveElectricalCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep

interface ElectricalSafetyState : JourneyState {
    val allowProvideCertificateLaterRoute: Boolean

    fun getElectricalCertificateExpiryDateIfReachable() =
        electricalCertExpiryDateStep.formModelIfReachableOrNull?.let { date ->
            DateTimeHelper.parseDateOrNull(date.day, date.month, date.year)
        }

    fun getElectricalCertificateIsOutdated(): Boolean? =
        getElectricalCertificateExpiryDateIfReachable()?.let { expiryDate ->
            DateTimeHelper().getCurrentDateInUK() >= expiryDate
        }

    fun getElectricalCertificateType(): HasElectricalSafetyCertificate? =
        hasElectricalCertStep.formModelIfReachableOrNull?.electricalCertType

    val electricalUploadIds: List<Long> get() =
        if (uploadElectricalCertStep.isStepReachable) {
            electricalUploadMap.values.map { it.fileUploadId }
        } else {
            emptyList()
        }

    val isOccupied: Boolean

    var electricalUploadMap: Map<Int, CertificateUpload>
    var highestAssignedElectricalMemberId: Int?

    fun getNextElectricalUploadMemberId(): Int = highestAssignedElectricalMemberId?.let { it + 1 } ?: 1

    val hasElectricalCertStep: HasElectricalCertStep
    val electricalCertExpiryDateStep: ElectricalCertExpiryDateStep
    val uploadElectricalCertStep: UploadElectricalCertStep
    val hasUploadedElectricalCert: HasAnyInCollectionStep
    val checkElectricalCertUploadsStep: CheckElectricalCertUploadsStep
    val removeElectricalCertUploadStep: RemoveElectricalCertUploadStep
    val electricalCertExpiredStep: ElectricalCertExpiredStep
    val electricalCertMissingStep: ElectricalCertMissingStep
    val provideElectricalCertLaterStep: ProvideElectricalCertLaterStep
    val checkElectricalSafetyAnswersStep: CheckElectricalSafetyAnswersStep
}
