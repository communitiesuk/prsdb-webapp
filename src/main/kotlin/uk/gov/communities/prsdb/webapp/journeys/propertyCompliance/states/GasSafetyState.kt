package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.yearsUntil
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
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
    val propertyId: Long

    fun getGasSafetyCertificateIssueDate() =
        gasSafetyIssueDateStep.formModelOrNull?.let { date ->
            DateTimeHelper.parseDateOrNull(date.day, date.month, date.year)
        }

    // TODO PDJB-467 add test for more of these
    fun getGasSafetyExpiryDate() = getGasSafetyCertificateIssueDate()?.plus(DatePeriod(years = GAS_SAFETY_CERT_VALIDITY_YEARS))

    fun getGasSafetyCertificateIsOutdated(): Boolean? =
        getGasSafetyCertificateIssueDate()?.let { issueDate ->
            val today = DateTimeHelper().getCurrentDateInUK()
            issueDate.yearsUntil(today) >= GAS_SAFETY_CERT_VALIDITY_YEARS
        }

    fun getGasSafetyCertificateFileUploadId(): Long? = gasSafetyCertificateUploadStep.formModelOrNull?.fileUploadId
}
