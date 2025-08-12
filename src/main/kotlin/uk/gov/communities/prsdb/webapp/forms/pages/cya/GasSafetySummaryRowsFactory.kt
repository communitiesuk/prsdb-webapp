package uk.gov.communities.prsdb.webapp.forms.pages.cya

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertEngineerNum
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertUploadId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyExemptionConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyExemptionMissing
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedGasSafetyUploadConfirmation
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyCertOutdated
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService

class GasSafetySummaryRowsFactory(
    val doesDataHaveGasSafetyCert: (JourneyData) -> Boolean,
    val gasSafetyStartingStep: PropertyComplianceStepId,
    val changeExemptionStep: PropertyComplianceStepId,
    val uploadService: UploadService,
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
        data class GasSafetyCertificateValue(
            val fieldValue: String,
            val downloadUrl: String? = null,
        )

        fun createGasSafetyCertificateValueForFileUpload(fileId: Long): GasSafetyCertificateValue {
            val fileUpload = uploadService.getFileUploadById(fileId)

            return when (fileUpload.status) {
                FileUploadStatus.QUARANTINED -> GasSafetyCertificateValue("forms.checkComplianceAnswers.gasSafety.notYetAvailable")
                FileUploadStatus.DELETED -> GasSafetyCertificateValue("forms.checkComplianceAnswers.gasSafety.virusScanFailed")
                FileUploadStatus.SCANNED ->
                    GasSafetyCertificateValue(
                        "forms.checkComplianceAnswers.gasSafety.download",
                        uploadService.getDownloadUrl(fileUpload, "gas_safety_certificate.${fileUpload.extension}"),
                    )
            }
        }

        val gasSafetyCertificateValue =
            when (GasSafetyStatus.fromJourneyData(filteredJourneyData)) {
                GasSafetyStatus.UPLOADED ->
                    createGasSafetyCertificateValueForFileUpload(
                        filteredJourneyData.getGasSafetyCertUploadId()!!.toLong(),
                    )
                GasSafetyStatus.EXEMPTION -> GasSafetyCertificateValue("forms.checkComplianceAnswers.certificate.notRequired")
                GasSafetyStatus.MISSING -> GasSafetyCertificateValue("forms.checkComplianceAnswers.certificate.notAdded")
                GasSafetyStatus.OUTDATED -> GasSafetyCertificateValue("forms.checkComplianceAnswers.certificate.expired")
            }

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.gasSafety.certificate",
            gasSafetyCertificateValue.fieldValue,
            gasSafetyStartingStep.urlPathSegment,
            gasSafetyCertificateValue.downloadUrl,
        )
    }

    private fun getGasSafetyCertDetailRows(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val issueDate = filteredJourneyData.getGasSafetyCertIssueDate()!!
                addAll(
                    listOf(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.certificate.issueDate",
                            issueDate,
                            PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment,
                        ),
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "forms.checkComplianceAnswers.certificate.validUntil",
                            issueDate.plus(DatePeriod(years = GAS_SAFETY_CERT_VALIDITY_YEARS)),
                            null,
                        ),
                    ),
                )

                val engineerNum = filteredJourneyData.getGasSafetyCertEngineerNum()
                if (engineerNum != null && !filteredJourneyData.getIsGasSafetyCertOutdated()!!) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
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

        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkComplianceAnswers.certificate.exemption",
            fieldValue,
            changeExemptionStep.urlPathSegment,
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
        fun fromJourneyData(data: JourneyData): GasSafetyStatus {
            val statusList =
                listOfNotNull(
                    if (data.getHasCompletedGasSafetyUploadConfirmation()) UPLOADED else null,
                    if (data.getHasCompletedGasSafetyExemptionConfirmation()) EXEMPTION else null,
                    if (data.getHasCompletedGasSafetyExemptionMissing()) MISSING else null,
                    if (data.getHasCompletedGasSafetyOutdated()) OUTDATED else null,
                )
            return statusList.singleOrNull()
                ?: throw PrsdbWebException("Filtered journey data does not have a single gas safety status: $statusList")
        }
    }
}
