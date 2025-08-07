package uk.gov.communities.prsdb.webapp.forms.pages.cya

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrUploadConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrOutdated
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class EicrSummaryRowsFactory(
    val doesDataHaveEicr: (JourneyData) -> Boolean,
    val eicrStartingStep: PropertyComplianceStepId,
    val changeExemptionStep: PropertyComplianceStepId,
) {
    fun createRows(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                add(getEicrStatusRow(filteredJourneyData))
                if (doesDataHaveEicr(filteredJourneyData)) {
                    addAll(getEicrDetailRows(filteredJourneyData))
                } else {
                    add(getEicrExemptionRow(filteredJourneyData))
                }
            }.toList()

    private fun getEicrStatusRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue =
            if (filteredJourneyData.getHasCompletedEicrExemptionMissing()) {
                "forms.checkComplianceAnswers.certificate.notAdded"
            } else if (filteredJourneyData.getHasCompletedEicrExemptionConfirmation()) {
                "forms.checkComplianceAnswers.certificate.notRequired"
            } else if (filteredJourneyData.getIsEicrOutdated() == true) {
                "forms.checkComplianceAnswers.certificate.expired"
            } else if (filteredJourneyData.getHasCompletedEicrUploadConfirmation()) {
                // TODO PRSD-976: Add link to gas safety cert (or appropriate message if virus scan failed)
                "forms.checkComplianceAnswers.eicr.download"
            } else {
                throw PrsdbWebException("Unexpected EICR status in journey data.")
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.eicr.certificate",
            fieldValue,
            eicrStartingStep.urlPathSegment,
        )
    }

    private fun getEicrDetailRows(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> {
        val issueDate = filteredJourneyData.getEicrIssueDate()!!
        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.certificate.issueDate",
                issueDate,
                PropertyComplianceStepId.EicrIssueDate.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkComplianceAnswers.certificate.validUntil",
                issueDate.plus(DatePeriod(years = EICR_VALIDITY_YEARS)),
                null,
            ),
        )
    }

    private fun getEicrExemptionRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue: Any =
            when (val exemptionReason = filteredJourneyData.getEicrExemptionReason()) {
                null -> "commonText.none"
                EicrExemptionReason.OTHER -> listOf(exemptionReason, filteredJourneyData.getEicrExemptionOtherReason())
                else -> exemptionReason
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.certificate.exemption",
            fieldValue,
            changeExemptionStep.urlPathSegment,
        )
    }
}
