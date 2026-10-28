package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.helpers.CertificateExpiryHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicrUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveEicUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveEicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicrStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask

interface ElectricalSafetyState : JourneyState {
    val electricalSafetyDetailsTask: ElectricalSafetyDetailsTask

    val checkElectricalSafetyAnswersStep: CheckElectricalSafetyAnswersStep
}

interface ElectricalSafetyDetailState : JourneyState {
    val hasElectricalCertStep: HasElectricalCertStep
    val eicExpiryDateStep: EicExpiryDateStep
    val uploadEicStep: UploadEicStep
    val hasUploadedEic: HasAnyInCollectionStep
    val checkEicUploadsStep: CheckEicUploadsStep
    val removeEicUploadStep: RemoveEicUploadStep
    val eicExpiredStep: EicExpiredStep
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

    fun getEicExpiryDateIfReachable() =
        eicExpiryDateStep.formModelIfReachableOrNull?.let { date ->
            DateTimeHelper.parseDateOrNull(date.day, date.month, date.year)
        }

    fun getEicrExpiryDateIfReachable() =
        eicrExpiryDateStep.formModelIfReachableOrNull?.let { date ->
            DateTimeHelper.parseDateOrNull(date.day, date.month, date.year)
        }

    // Only one of the EIC and EICR branches can be reachable at a time
    fun getElectricalCertificateExpiryDateIfReachable() = getEicExpiryDateIfReachable() ?: getEicrExpiryDateIfReachable()

    fun getEicIsOutdated(): Boolean? =
        getEicExpiryDateIfReachable()?.let { CertificateExpiryHelper.isEicExpiryDateExpired(it.toJavaLocalDate()) }

    fun getEicrIsOutdated(): Boolean? =
        getEicrExpiryDateIfReachable()?.let { CertificateExpiryHelper.isEicrExpiryDateExpired(it.toJavaLocalDate()) }

    fun getElectricalCertificateIsOutdated(): Boolean? = getEicIsOutdated() ?: getEicrIsOutdated()

    fun getElectricalCertificateType(): HasElectricalSafetyCertificate? =
        hasElectricalCertStep.formModelIfReachableOrNull?.electricalCertType

    fun mapElectricalCertificateTypeToGlobalCertificateType(): CertificateType? =
        when (getElectricalCertificateType()) {
            HasElectricalSafetyCertificate.HAS_EIC -> CertificateType.Eic
            HasElectricalSafetyCertificate.HAS_EICR -> CertificateType.Eicr
            else -> null
        }

    val electricalUploadIds: List<Long>
        get() =
            when {
                uploadEicStep.isStepReachable -> eicUploadMap.values.map { it.fileUploadId }
                uploadEicrStep.isStepReachable -> eicrUploadMap.values.map { it.fileUploadId }
                else -> emptyList()
            }

    var eicUploadMap: Map<Int, CertificateUpload>
    var highestAssignedEicMemberId: Int?

    fun getNextEicUploadMemberId(): Int = highestAssignedEicMemberId?.let { it + 1 } ?: 1

    var eicrUploadMap: Map<Int, CertificateUpload>
    var highestAssignedEicrMemberId: Int?

    fun getNextEicrUploadMemberId(): Int = highestAssignedEicrMemberId?.let { it + 1 } ?: 1
}
