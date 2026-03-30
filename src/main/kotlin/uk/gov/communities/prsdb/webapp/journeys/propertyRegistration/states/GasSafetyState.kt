package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
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

    fun getGasSafetyCertificateIssueDateIfReachable() =
        gasCertIssueDateStep.formModelIfReachableOrNull?.let { date ->
            DateTimeHelper.parseDateOrNull(date.day, date.month, date.year)
        }

    fun getGasSafetyCertificateIsOutdated(): Boolean? =
        getGasSafetyCertificateIssueDateIfReachable()?.let { issueDate ->
            DateTimeHelper().getCurrentDateInUK() > issueDate.plus(DatePeriod(years = GAS_SAFETY_CERT_VALIDITY_YEARS))
        }

    val gasUploadId: Long? get() = uploadGasCertStep.formModelIfReachableOrNull?.fileUploadId

    var gasUploadMap: Map<Int, GasSafetyUpload>?
    var nextGasUploadMemberId: Int?

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

@Serializable
data class GasSafetyUpload(
    val fileUploadId: Long,
    val fileName: String,
)
