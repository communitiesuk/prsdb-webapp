package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService

class EicrCyaSummaryRowsFactory(
    val propertyHasEicr: Boolean,
    val eicrStartingStep: Destination.VisitableStep,
    val changeExemptionStep: Destination.VisitableStep,
    val uploadService: UploadService,
    val state: EicrState,
    val childJourneyId: String,
) {
    fun createRows() =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                add(getEicrStatusRow())
                if (propertyHasEicr) {
                    addAll(getEicrDetailRows())
                } else {
                    add(getEicrExemptionRow())
                }
            }

    private fun getEicrStatusRow(): SummaryListRowViewModel {
        data class EicrValue(
            val fieldValue: String,
            val downloadUrl: String? = null,
        )

        fun createEicrValueForFileUpload(fileId: Long): EicrValue {
            val fileUpload = uploadService.getFileUploadById(fileId)

            return when (fileUpload.status) {
                FileUploadStatus.QUARANTINED -> {
                    EicrValue("propertyCompliance.uploadedFile.virusScanPending")
                }

                FileUploadStatus.DELETED -> {
                    EicrValue("propertyCompliance.uploadedFile.virusScanFailed")
                }

                FileUploadStatus.SCANNED -> {
                    EicrValue(
                        "forms.checkComplianceAnswers.eicr.download",
                        uploadService.getDownloadUrl(fileUpload, "eicr.${fileUpload.extension}"),
                    )
                }
            }
        }

        val eicrValue =
            when (EicrStatus.fromState(state)) {
                EicrStatus.UPLOADED -> {
                    createEicrValueForFileUpload(
                        state.getEicrCertificateFileUploadId()!!,
                    )
                }

                EicrStatus.EXEMPTION -> {
                    EicrValue("forms.checkComplianceAnswers.certificate.notRequired")
                }

                EicrStatus.MISSING -> {
                    EicrValue("forms.checkComplianceAnswers.certificate.notAdded")
                }

                EicrStatus.OUTDATED -> {
                    EicrValue("forms.checkComplianceAnswers.certificate.expired")
                }
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.eicr.certificate",
            eicrValue.fieldValue,
            eicrStartingStep,
            eicrValue.downloadUrl,
        )
    }

    private fun getEicrDetailRows() =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val issueDate = state.getEicrCertificateIssueDate()!!
                addAll(
                    listOf(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.certificate.issueDate",
                            issueDate,
                            Destination.VisitableStep(state.eicrIssueDateStep, childJourneyId),
                        ),
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.certificate.validUntil",
                            issueDate.plus(DatePeriod(years = EICR_VALIDITY_YEARS)),
                            null,
                        ),
                    ),
                )
            }.toList()

    private fun getEicrExemptionRow(): SummaryListRowViewModel {
        val exemptionReason = state.eicrExemptionReasonStep.formModelIfReachableOrNull?.exemptionReason
        val fieldValue =
            when (exemptionReason) {
                null -> {
                    "commonText.none"
                }

                EicrExemptionReason.OTHER -> {
                    listOf(
                        exemptionReason,
                        state.eicrExemptionOtherReasonStep.formModel.otherReason,
                    )
                }

                else -> {
                    exemptionReason
                }
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.certificate.exemption",
            fieldValue,
            changeExemptionStep,
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
        fun fromState(state: EicrState): EicrStatus {
            val statusList =
                listOfNotNull(
                    if (state.eicrUploadConfirmationStep.outcome == Complete.COMPLETE) UPLOADED else null,
                    if (state.eicrExemptionConfirmationStep.outcome == Complete.COMPLETE) EXEMPTION else null,
                    if (state.eicrExemptionMissingStep.outcome == Complete.COMPLETE) MISSING else null,
                    if (state.eicrOutdatedStep.outcome == Complete.COMPLETE) OUTDATED else null,
                )
            return statusList.singleOrNull()
                ?: throw PrsdbWebException("Filtered journey data does not have a single EICR status: $statusList")
        }
    }
}
