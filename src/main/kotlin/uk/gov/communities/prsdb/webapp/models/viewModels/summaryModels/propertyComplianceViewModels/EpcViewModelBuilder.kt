package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.constants.EPC_ACCEPTABLE_RATING_RANGE
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class EpcViewModelBuilder {
    companion object {
        fun fromEntity(
            propertyCompliance: PropertyCompliance,
            withActionLinks: Boolean,
        ): List<SummaryListRowViewModel> =
            mutableListOf<SummaryListRowViewModel>()
                .apply {
                    addRow(
                        key = "propertyDetails.complianceInformation.energyPerformance.epc",
                        value = getEpcMessageKey(propertyCompliance),
                        valueUrl = propertyCompliance.epcUrl,
                        actionText = "forms.links.change",
                        // TODO PRSD-1312 add Update EPC Compliance Link
                        actionLink = "#",
                        withActionLink = withActionLinks,
                    )
                    if (propertyCompliance.epcUrl != null) {
                        addRow(
                            key = "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                            value = propertyCompliance.epcExpiryDate,
                        )
                        addRow(
                            key = "propertyDetails.complianceInformation.energyPerformance.energyRating",
                            value = propertyCompliance.epcEnergyRating?.uppercase(),
                        )
                    } else {
                        addRow(
                            key = "propertyDetails.complianceInformation.exemption",
                            value = getEpcExemptionReasonValue(propertyCompliance.epcExemptionReason),
                        )
                    }
                    if (propertyCompliance.isEpcExpired == true) {
                        addRow(
                            key = "propertyDetails.complianceInformation.energyPerformance.didTenancyStartBeforeEpcExpired",
                            value = MessageKeyConverter.convert(propertyCompliance.tenancyStartedBeforeEpcExpiry!!),
                        )
                    }
                    if (shouldAddMeesExemptionRow(propertyCompliance)) {
                        addRow(
                            key = "propertyDetails.complianceInformation.energyPerformance.meesExemption",
                            value = getMeesExemptionReasonValue(propertyCompliance.epcMeesExemptionReason),
                            // TODO PRSD-1312 add Update EPC Compliance Link
                            actionLink = "#",
                            withActionLink = withActionLinks,
                        )
                    }
                }.toList()

        private fun getEpcMessageKey(propertyCompliance: PropertyCompliance): String =
            if (propertyCompliance.epcUrl != null) {
                if (propertyCompliance.isEpcExpired!!) {
                    "propertyDetails.complianceInformation.energyPerformance.viewExpiredEpcLinkText"
                } else {
                    "propertyDetails.complianceInformation.energyPerformance.viewEpcLinkText"
                }
            } else {
                if (propertyCompliance.hasEpcExemption) {
                    "propertyDetails.complianceInformation.notRequired"
                } else {
                    "propertyDetails.complianceInformation.notAdded"
                }
            }

        private fun getEpcExemptionReasonValue(exemptionReason: EpcExemptionReason?): String =
            if (exemptionReason != null) {
                MessageKeyConverter.convert(exemptionReason)
            } else {
                "propertyDetails.complianceInformation.noExemption"
            }

        private fun getMeesExemptionReasonValue(exemptionReason: MeesExemptionReason?): String =
            if (exemptionReason != null) {
                MessageKeyConverter.convert(exemptionReason)
            } else {
                "commonText.none"
            }

        private fun shouldAddMeesExemptionRow(propertyCompliance: PropertyCompliance): Boolean =
            propertyCompliance.epcMeesExemptionReason != null ||
                propertyCompliance.epcEnergyRating != null &&
                propertyCompliance.epcEnergyRating!!.uppercase() !in EPC_ACCEPTABLE_RATING_RANGE
    }
}
