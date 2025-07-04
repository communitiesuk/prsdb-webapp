package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class GasSafetyViewModelBuilder {
    companion object {
        fun fromEntity(
            propertyCompliance: PropertyCompliance,
            withActionLinks: Boolean,
        ): List<SummaryListRowViewModel> =
            mutableListOf<SummaryListRowViewModel>()
                .apply {
                    addRow(
                        key = "propertyDetails.complianceInformation.gasSafety.gasSafetyCertificate",
                        value = getGasCertificateMessageKey(propertyCompliance),
                        valueUrl = getDownloadLinkOrNull(propertyCompliance.hasGasSafetyCert),
                        actionText = "forms.links.change",
                        // TODO PRSD-1244 add Update Gas Compliance Link
                        actionLink = "#",
                        withActionLink = withActionLinks,
                    )
                    if (propertyCompliance.gasSafetyCertIssueDate != null) {
                        addRow(
                            key = "propertyDetails.complianceInformation.issueDate",
                            value = propertyCompliance.gasSafetyCertIssueDate,
                        )
                        addRow(
                            key = "propertyDetails.complianceInformation.validUntil",
                            value = propertyCompliance.gasSafetyCertExpiryDate,
                        )
                        addRow(
                            key = "propertyDetails.complianceInformation.gasSafety.gasSafeEngineerNumber",
                            value = propertyCompliance.gasSafetyCertEngineerNum,
                        )
                    } else {
                        addRow(
                            key = "propertyDetails.complianceInformation.exemption",
                            value = getExemptionReasonValue(propertyCompliance),
                        )
                    }
                }.toList()

        private fun getGasCertificateMessageKey(propertyCompliance: PropertyCompliance): String =
            if (propertyCompliance.hasGasSafetyCert) {
                if (propertyCompliance.isGasSafetyCertExpired!!) {
                    "propertyDetails.complianceInformation.gasSafety.downloadExpiredCertificate"
                } else {
                    "propertyDetails.complianceInformation.gasSafety.downloadCertificate"
                }
            } else if (propertyCompliance.gasSafetyCertIssueDate != null) {
                "propertyDetails.complianceInformation.expired"
            } else if (propertyCompliance.hasGasSafetyExemption) {
                "propertyDetails.complianceInformation.exempt"
            } else {
                "propertyDetails.complianceInformation.notAdded"
            }

        private fun getExemptionReasonValue(propertyCompliance: PropertyCompliance): Any =
            when (propertyCompliance.gasSafetyCertExemptionReason) {
                null -> "propertyDetails.complianceInformation.noExemption"
                GasSafetyExemptionReason.OTHER ->
                    listOf(
                        MessageKeyConverter.convert(GasSafetyExemptionReason.OTHER),
                        propertyCompliance.gasSafetyCertExemptionOtherReason,
                    )

                else -> MessageKeyConverter.convert(propertyCompliance.gasSafetyCertExemptionReason!!)
            }

        // TODO PRSD-976 add link to download certificate and appropriate messages when required
        private fun getDownloadLinkOrNull(hasCert: Boolean): String? = if (hasCert) "#" else null
    }
}
