package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService

class GasSafetyCyaSummaryRowsFactory(
    val propertyHasGasSafetyCertificate: Boolean,
    val gasSafetyStartingStep: Destination.VisitableStep,
    val changeExemptionStep: Destination.VisitableStep,
    val uploadService: UploadService,
    val state: GasSafetyState,
    val childJourneyId: String,
) {
    fun createRows() =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                add(getGasSafetyCertStatusRow())
                if (propertyHasGasSafetyCertificate) {
                    addAll(getGasSafetyCertDetailRows())
                } else {
                    add(getGasSafetyExemptionRow())
                }
            }

    private fun getGasSafetyCertStatusRow(): SummaryListRowViewModel {
        data class GasSafetyCertificateValue(
            val fieldValue: String,
            val downloadUrl: String? = null,
        )

        fun createGasSafetyCertificateValueForFileUpload(fileId: Long): GasSafetyCertificateValue {
            val fileUpload = uploadService.getFileUploadById(fileId)

            return when (fileUpload.status) {
                FileUploadStatus.QUARANTINED -> {
                    GasSafetyCertificateValue("propertyCompliance.uploadedFile.virusScanPending")
                }

                FileUploadStatus.DELETED -> {
                    GasSafetyCertificateValue("propertyCompliance.uploadedFile.virusScanFailed")
                }

                FileUploadStatus.SCANNED -> {
                    GasSafetyCertificateValue(
                        "forms.checkComplianceAnswers.gasSafety.download",
                        uploadService.getDownloadUrl(fileUpload, "gas_safety_certificate.${fileUpload.extension}"),
                    )
                }
            }
        }

        val gasSafetyCertificateValue =
            when (GasSafetyStatus.fromState(state)) {
                GasSafetyStatus.UPLOADED -> {
                    createGasSafetyCertificateValueForFileUpload(
                        state.getGasSafetyCertificateFileUploadIdIfReachable()!!,
                    )
                }

                GasSafetyStatus.EXEMPTION -> {
                    GasSafetyCertificateValue("forms.checkComplianceAnswers.certificate.notRequired")
                }

                GasSafetyStatus.MISSING -> {
                    GasSafetyCertificateValue("forms.checkComplianceAnswers.certificate.notAdded")
                }

                GasSafetyStatus.OUTDATED -> {
                    GasSafetyCertificateValue("forms.checkComplianceAnswers.certificate.expired")
                }
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.gasSafety.certificate",
            gasSafetyCertificateValue.fieldValue,
            gasSafetyStartingStep,
            gasSafetyCertificateValue.downloadUrl,
        )
    }

    private fun getGasSafetyCertDetailRows() =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val issueDate = state.getGasSafetyCertificateIssueDateIfReachable()
                addAll(
                    listOf(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.certificate.issueDate",
                            issueDate,
                            Destination.VisitableStep(state.gasSafetyIssueDateStep, childJourneyId),
                        ),
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.certificate.validUntil",
                            state.getGasSafetyExpiryDate(),
                            null,
                        ),
                    ),
                )

                val engineerNum = state.gasSafetyEngineerNumberStep.formModelOrNull?.engineerNumber
                if (engineerNum != null && !state.getGasSafetyCertificateIsOutdated()!!) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.gasSafety.engineerNumber",
                            engineerNum,
                            Destination.VisitableStep(state.gasSafetyEngineerNumberStep, childJourneyId),
                        ),
                    )
                }
            }.toList()

    private fun getGasSafetyExemptionRow(): SummaryListRowViewModel {
        val exemptionReason = state.gasSafetyExemptionReasonStep.formModelOrNull?.exemptionReason
        val fieldValue =
            if ((state.gasSafetyExemptionStep.formModelOrNull?.hasExemption != true) || exemptionReason == null) {
                "commonText.none"
            } else if (exemptionReason == GasSafetyExemptionReason.OTHER) {
                listOf(
                    exemptionReason,
                    state.gasSafetyExemptionOtherReasonStep.formModelOrNull?.otherReason,
                )
            } else {
                exemptionReason
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.certificate.exemption",
            fieldValue,
            changeExemptionStep,
        )
    }
}

private enum class GasSafetyStatus {
    UPLOADED,
    EXEMPTION,
    MISSING,
    OUTDATED,
    ;

    companion object {
        fun fromState(state: GasSafetyState): GasSafetyStatus {
            val statusList =
                listOfNotNull(
                    if (state.gasSafetyUploadConfirmationStep.outcome == Complete.COMPLETE) UPLOADED else null,
                    if (state.gasSafetyExemptionConfirmationStep.outcome == Complete.COMPLETE) EXEMPTION else null,
                    if (state.gasSafetyExemptionMissingStep.outcome == Complete.COMPLETE) MISSING else null,
                    if (state.gasSafetyOutdatedStep.outcome == Complete.COMPLETE) OUTDATED else null,
                )
            return statusList.singleOrNull()
                ?: throw PrsdbWebException("Filtered journey data does not have a single gas safety status: $statusList")
        }
    }
}
