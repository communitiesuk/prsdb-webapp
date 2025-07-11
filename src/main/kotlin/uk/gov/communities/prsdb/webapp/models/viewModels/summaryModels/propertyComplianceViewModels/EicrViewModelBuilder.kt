package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
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
                        // TODO PRSD-1246 add Update EICR Compliance Link
                        actionLink = "#",
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
                            value = getExemptionReasonValue(propertyCompliance.eicrExemptionReason),
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

        private fun getExemptionReasonValue(exemptionReason: EicrExemptionReason?): Any =
            when (exemptionReason) {
                null -> "propertyDetails.complianceInformation.noExemption"
                EicrExemptionReason.OTHER -> listOf(MessageKeyConverter.convert(EicrExemptionReason.OTHER), exemptionReason)
                else -> MessageKeyConverter.convert(exemptionReason)
            }

        // TODO PRSD-976 add link to download certificate and appropriate messages when required
        private fun getDownloadLinkOrNull(hasCert: Boolean): String? = if (hasCert) "#" else null
    }
}
