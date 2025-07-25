package uk.gov.communities.prsdb.webapp.forms.pages.cya

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertEngineerNum
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyExemptionMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyUploadConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class GasSafetySummaryRowsFactory(
    val doesDataHaveGasSafetyCert: (JourneyData) -> Boolean,
    val gasSafetyStartingStep: PropertyComplianceStepId,
    val changeExemptionStep: PropertyComplianceStepId,
) {
    fun createRows(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                add(getGasSafetyCertStatusRow(filteredJourneyData))
                if (doesDataHaveGasSafetyCert(filteredJourneyData)) {
                    addAll(getGasSafetyCertDetailRows(filteredJourneyData))
                } else {
                    add(getGasSafetyExemptionRow(filteredJourneyData))
                }
            }.toList()

    private fun getGasSafetyCertStatusRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue =
            // TODO PRSD-976: Add link to gas safety cert (or appropriate message if virus scan failed)
            if (filteredJourneyData.getHasCompletedGasSafetyUploadConfirmation()) {
                "forms.checkComplianceAnswers.gasSafety.download"
            } else if (filteredJourneyData.getHasCompletedGasSafetyExemptionConfirmation()) {
                "forms.checkComplianceAnswers.certificate.notRequired"
            } else if (filteredJourneyData.getHasCompletedGasSafetyExemptionMissing()) {
                "forms.checkComplianceAnswers.certificate.notAdded"
            } else {
                "forms.checkComplianceAnswers.certificate.expired"
            }

        return SummaryListRowViewModel.Companion.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.gasSafety.certificate",
            fieldValue,
            gasSafetyStartingStep.urlPathSegment,
        )
    }

    private fun getGasSafetyCertDetailRows(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val issueDate = filteredJourneyData.getGasSafetyCertIssueDate()!!
                addAll(
                    listOf(
                        SummaryListRowViewModel.Companion.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.certificate.issueDate",
                            issueDate,
                            PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment,
                        ),
                        SummaryListRowViewModel.Companion.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.certificate.validUntil",
                            issueDate.plus(DatePeriod(years = GAS_SAFETY_CERT_VALIDITY_YEARS)),
                            null,
                        ),
                    ),
                )

                val engineerNum = filteredJourneyData.getGasSafetyCertEngineerNum()
                if (engineerNum != null) {
                    add(
                        SummaryListRowViewModel.Companion.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.gasSafety.engineerNumber",
                            engineerNum,
                            PropertyComplianceStepId.GasSafetyEngineerNum.urlPathSegment,
                        ),
                    )
                }
            }.toList()

    private fun getGasSafetyExemptionRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val fieldValue: Any =
            when (val exemptionReason = filteredJourneyData.getGasSafetyCertExemptionReason()) {
                null -> "commonText.none"
                GasSafetyExemptionReason.OTHER -> listOf(exemptionReason, filteredJourneyData.getGasSafetyCertExemptionOtherReason())
                else -> exemptionReason
            }

        return SummaryListRowViewModel.Companion.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.certificate.exemption",
            fieldValue,
            changeExemptionStep.urlPathSegment,
        )
    }
}
