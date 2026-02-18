package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import kotlinx.datetime.yearsUntil
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState

interface EicrState :
    JourneyState,
    CheckYourAnswersJourneyState {
    val eicrStep: EicrStep
    val eicrIssueDateStep: EicrIssueDateStep
    val eicrUploadStep: EicrUploadStep
    val eicrUploadConfirmationStep: EicrUploadConfirmationStep
    val eicrOutdatedStep: EicrOutdatedStep
    val eicrExemptionStep: EicrExemptionStep
    val eicrExemptionReasonStep: EicrExemptionReasonStep
    val eicrExemptionOtherReasonStep: EicrExemptionOtherReasonStep
    val eicrExemptionConfirmationStep: EicrExemptionConfirmationStep
    val eicrExemptionMissingStep: EicrExemptionMissingStep
    val propertyId: Long

    fun getEicrCertificateIssueDate() =
        eicrIssueDateStep.formModelIfReachableOrNull?.let { date ->
            DateTimeHelper.parseDateOrNull(date.day, date.month, date.year)
        }

    fun getEicrCertificateIsOutdated() =
        getEicrCertificateIssueDate()?.let { issueDate ->
            val today = DateTimeHelper().getCurrentDateInUK()
            issueDate.yearsUntil(today) >= EICR_VALIDITY_YEARS
        }

    fun getEicrCertificateFileUploadId() = eicrUploadStep.formModelIfReachableOrNull?.fileUploadId
}
