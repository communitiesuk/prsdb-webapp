package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class EicrViewModelBuilder {
    companion object {
        fun fromEntity(
            propertyCompliance: PropertyCompliance,
            withActionLinks: Boolean,
        ): List<SummaryListRowViewModel> =
            mutableListOf<SummaryListRowViewModel>()
                .apply {
                    addRow(
                        key = "propertyDetails.complianceInformation.electricalSafety.eicr",
                        // TODO PRSD-976 add link to download certificate and appropriate messages when required
                        value = getEicrMessageKey(propertyCompliance),
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

        private fun getEicrMessageKey(propertyCompliance: PropertyCompliance): String =
            if (propertyCompliance.eicrS3Key != null) {
                if (propertyCompliance.isEicrExpired!!) {
                    "propertyDetails.complianceInformation.electricalSafety.downloadExpiredEicr"
                } else {
                    "propertyDetails.complianceInformation.electricalSafety.downloadEicr"
                }
            } else if (propertyCompliance.eicrIssueDate != null) {
                "propertyDetails.complianceInformation.expired"
            } else if (propertyCompliance.hasEicrExemption) {
                "propertyDetails.complianceInformation.exempt"
            } else {
                "propertyDetails.complianceInformation.notAdded"
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

        // TODO PRSD-976 add link to download certificate and appropriate messages when required
        private fun getDownloadLinkOrNull(hasCert: Boolean): String? = if (hasCert) "#" else null
    }
}
