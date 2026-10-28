package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.helpers.CertificateExpiryHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicrUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveEicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicrStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask

interface ElectricalSafetyState : JourneyState {
    val electricalSafetyDetailsTask: ElectricalSafetyDetailsTask

    val checkElectricalSafetyAnswersStep: CheckElectricalSafetyAnswersStep
}

interface ElectricalSafetyDetailState : JourneyState {
    val hasElectricalCertStep: HasElectricalCertStep
    val eicrExpiryDateStep: EicrExpiryDateStep
    val uploadEicrStep: UploadEicrStep
    val hasUploadedEicr: HasAnyInCollectionStep
    val checkEicrUploadsStep: CheckEicrUploadsStep
    val removeEicrUploadStep: RemoveEicrUploadStep
    val eicrExpiredStep: EicrExpiredStep
    val electricalCertMissingStep: ElectricalCertMissingStep
    val provideElectricalCertLaterStep: ProvideElectricalCertLaterStep

    val isOccupied: Boolean
    val allowProvideCertificateLaterRoute: Boolean
    val propertyOwnershipId: Long?

    fun getElectricalCertificateExpiryDateIfReachable() =
        eicrExpiryDateStep.formModelIfReachableOrNull?.let { date ->
            DateTimeHelper.parseDateOrNull(date.day, date.month, date.year)
        }

    fun getElectricalCertificateIsOutdated(): Boolean? =
        getElectricalCertificateExpiryDateIfReachable()?.let { expiryDate ->
            when (val certType = getElectricalCertificateTypeAsCertificateType()) {
                CertificateType.Eic -> CertificateExpiryHelper.isEicExpiryDateExpired(expiryDate.toJavaLocalDate())
                CertificateType.Eicr -> CertificateExpiryHelper.isEicrExpiryDateExpired(expiryDate.toJavaLocalDate())
                else -> throw PrsdbWebException("Electrical cert type must be EIC or EICR but was $certType")
            }
        }

    fun getElectricalCertificateType(): HasElectricalSafetyCertificate? =
        hasElectricalCertStep.formModelIfReachableOrNull?.electricalCertType

    fun getElectricalCertificateTypeAsCertificateType(): CertificateType? =
        when (getElectricalCertificateType()) {
            HasElectricalSafetyCertificate.HAS_EIC -> CertificateType.Eic
            HasElectricalSafetyCertificate.HAS_EICR -> CertificateType.Eicr
            else -> null
        }

    fun mapElectricalCertificateTypeToGlobalCertificateType(): CertificateType? =
        when (getElectricalCertificateType()) {
            HasElectricalSafetyCertificate.HAS_EIC -> CertificateType.Eic
            HasElectricalSafetyCertificate.HAS_EICR -> CertificateType.Eicr
            else -> null
        }

    val electricalUploadIds: List<Long>
        get() =
            if (uploadEicrStep.isStepReachable) {
                eicrUploadMap.values.map { it.fileUploadId }
            } else {
                emptyList()
            }
    var eicrUploadMap: Map<Int, CertificateUpload>
    var highestAssignedEicrMemberId: Int?

    fun getNextEicrUploadMemberId(): Int = highestAssignedEicrMemberId?.let { it + 1 } ?: 1
}
