package uk.gov.communities.prsdb.webapp.forms.pages.cya

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrUploadConfirmation
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
            when (EicrStatus.fromJourneyData(filteredJourneyData)) {
                // TODO PRSD-976: Add link to EICR (or appropriate message if virus scan failed)
                EicrStatus.UPLOADED -> "forms.checkComplianceAnswers.eicr.download"
                EicrStatus.EXEMPTION -> "forms.checkComplianceAnswers.certificate.notRequired"
                EicrStatus.MISSING -> "forms.checkComplianceAnswers.certificate.notAdded"
                EicrStatus.OUTDATED -> "forms.checkComplianceAnswers.certificate.expired"
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

private enum class EicrStatus {
    UPLOADED,
    EXEMPTION,
    MISSING,
    OUTDATED,
    ;

    companion object {
        fun fromJourneyData(data: JourneyData): EicrStatus =
            listOfNotNull(
                if (data.getHasCompletedEicrUploadConfirmation()) UPLOADED else null,
                if (data.getHasCompletedEicrExemptionConfirmation()) EXEMPTION else null,
                if (data.getHasCompletedEicrExemptionMissing()) MISSING else null,
                if (data.getHasCompletedEicrOutdated()) OUTDATED else null,
            ).single()
    }
}
