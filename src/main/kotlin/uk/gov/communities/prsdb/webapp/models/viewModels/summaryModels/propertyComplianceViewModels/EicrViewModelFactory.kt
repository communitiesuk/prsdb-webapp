package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService

@PrsdbWebService
class EicrViewModelFactory(
    private val uploadService: UploadService,
) {
    fun fromEntity(
        propertyCompliance: PropertyCompliance,
        withActionLinks: Boolean,
    ): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    key = "propertyDetails.complianceInformation.electricalSafety.eicr",
                    value = getEicrMessageKey(propertyCompliance),
                    valueUrl =
                        propertyCompliance.eicrFileUpload?.let {
                            uploadService.getDownloadUrlOrNull(it, "eicr.${it.extension}")
                        },
                    actionText = "forms.links.change",
                    actionLink =
                        PropertyComplianceController.getUpdatePropertyComplianceStepPath(
                            propertyCompliance.propertyOwnership.id,
                            PropertyComplianceStepId.UpdateEICR,
                        ),
                    withActionLink = withActionLinks,
                )
                if (propertyCompliance.eicrIssueDate != null) {
                    addRow(
                        key = "propertyDetails.complianceInformation.issueDate",
                        value = propertyCompliance.eicrIssueDate,
                    )
                    addRow(
                        key = "propertyDetails.complianceInformation.validUntil",
                        value = propertyCompliance.eicrExpiryDate,
                    )
                } else {
                    addRow(
                        key = "propertyDetails.complianceInformation.exemption",
                        value =
                            getExemptionReasonValue(
                                propertyCompliance.eicrExemptionReason,
                                propertyCompliance.eicrExemptionOtherReason,
                            ),
                    )
                }
            }.toList()

    private fun getEicrMessageKey(propertyCompliance: PropertyCompliance): String {
        val uploadedFileStatus = propertyCompliance.eicrFileUpload?.status
        val expired = propertyCompliance.isEicrExpired
        return when {
            uploadedFileStatus == FileUploadStatus.SCANNED && !expired!! ->
                "propertyDetails.complianceInformation.electricalSafety.downloadEicr"

            uploadedFileStatus == FileUploadStatus.SCANNED && expired!! ->
                "propertyDetails.complianceInformation.electricalSafety.downloadExpiredEicr"

            uploadedFileStatus == FileUploadStatus.QUARANTINED ->
                "propertyDetails.complianceInformation.electricalSafety.virusScanPending"

            uploadedFileStatus == FileUploadStatus.DELETED ->
                "propertyDetails.complianceInformation.electricalSafety.virusScanFailed"

            expired == true ->
                "propertyDetails.complianceInformation.expired"

            propertyCompliance.hasEicrExemption ->
                "propertyDetails.complianceInformation.exempt"

            else ->
                "propertyDetails.complianceInformation.notAdded"
        }
    }

    private fun getExemptionReasonValue(
        exemptionReason: EicrExemptionReason?,
        exemptionOtherReason: String?,
    ): Any =
        when (exemptionReason) {
            null -> "propertyDetails.complianceInformation.noExemption"
            EicrExemptionReason.OTHER -> listOf(MessageKeyConverter.convert(EicrExemptionReason.OTHER), exemptionOtherReason)
            else -> MessageKeyConverter.convert(exemptionReason)
        }
}
