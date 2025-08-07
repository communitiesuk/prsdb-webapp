package uk.gov.communities.prsdb.webapp.forms.pages.cya

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrUploadId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrExemptionMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEicrUploadConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService

class EicrSummaryRowsFactory(
    val doesDataHaveEicr: (JourneyData) -> Boolean,
    val eicrStartingStep: PropertyComplianceStepId,
    val changeExemptionStep: PropertyComplianceStepId,
    val uploadService: UploadService,
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
        data class EicrValue(
            val fieldValue: String,
            val downloadUrl: String? = null,
        )

        fun createEicrValueForFileUpload(fileId: Long): EicrValue {
            val fileUpload = uploadService.getFileUploadById(fileId)

            return when (fileUpload.status) {
                FileUploadStatus.QUARANTINED -> EicrValue("forms.checkComplianceAnswers.eicr.notYetAvailable")
                FileUploadStatus.DELETED -> EicrValue("forms.checkComplianceAnswers.eicr.virusScanFailed")
                FileUploadStatus.SCANNED ->
                    EicrValue(
                        "forms.checkComplianceAnswers.eicr.download",
                        uploadService.getDownloadUrl(fileUpload, "eicr.${fileUpload.extension}"),
                    )
            }
        }

        val eicrValue =
            when (EicrStatus.fromJourneyData(filteredJourneyData)) {
                EicrStatus.UPLOADED -> createEicrValueForFileUpload(filteredJourneyData.getEicrUploadId()!!.toLong())
                EicrStatus.EXEMPTION -> EicrValue("forms.checkComplianceAnswers.certificate.notRequired")
                EicrStatus.MISSING -> EicrValue("forms.checkComplianceAnswers.certificate.notAdded")
                EicrStatus.OUTDATED -> EicrValue("forms.checkComplianceAnswers.certificate.expired")
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.eicr.certificate",
            eicrValue.fieldValue,
            eicrStartingStep.urlPathSegment,
            eicrValue.downloadUrl,
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
        fun fromJourneyData(data: JourneyData): EicrStatus {
            val statusList =
                listOfNotNull(
                    if (data.getHasCompletedEicrUploadConfirmation()) UPLOADED else null,
                    if (data.getHasCompletedEicrExemptionConfirmation()) EXEMPTION else null,
                    if (data.getHasCompletedEicrExemptionMissing()) MISSING else null,
                    if (data.getHasCompletedEicrOutdated()) OUTDATED else null,
                )
            return statusList.singleOrNull()
                ?: throw PrsdbWebException("Filtered journey data does not have a single EICR status: $statusList")
        }
    }
}
