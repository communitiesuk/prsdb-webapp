package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.helpers.extensions.isEpcNonExpiredButLowRating
import uk.gov.communities.prsdb.webapp.helpers.extensions.isEpcValidDespiteExpiry
import uk.gov.communities.prsdb.webapp.helpers.extensions.shouldShowCouncilWillSeeEpcInset
import uk.gov.communities.prsdb.webapp.helpers.extensions.shouldShowEpcBecameExpiredInset
import uk.gov.communities.prsdb.webapp.helpers.extensions.shouldShowEpcMeesSection
import uk.gov.communities.prsdb.webapp.helpers.extensions.shouldShowEpcTenancySection
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.EpcExpiredInsetViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardSupplementarySection
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.TagValue

@PrsdbWebService("epcViewModelServiceRedesign")
class EpcViewModelFactory(
    messageSource: MessageSource,
) : ComplianceViewModelServiceBase(messageSource),
    EpcViewModelService {
    override val provideLaterUnoccupiedKey = "propertyDetails.complianceInformation.energyPerformance.provideEpcLaterUnoccupied"
    override val provideLaterWithDeadlineKey = "propertyDetails.complianceInformation.energyPerformance.occupiedWithDeadline"
    override val missingCertOccupiedValue = "commonText.no"
    override val occupiedNoCertInsetKey = "propertyDetails.complianceInformation.energyPerformance.occupiedNoEpcInset"

    override fun getStatus(propertyCompliance: PropertyCompliance): ComplianceCertStatus =
        ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).epcStatusMay2026Redesign

    override fun getInsetTextKey(propertyCompliance: PropertyCompliance): String? =
        if (propertyCompliance.shouldShowCouncilWillSeeEpcInset) occupiedNoCertInsetKey else null

    override fun getEpcExpiredInsetViewModel(propertyCompliance: PropertyCompliance): EpcExpiredInsetViewModel? {
        if (!propertyCompliance.shouldShowEpcBecameExpiredInset) return null

        val formattedDate = propertyCompliance.epcExpiryDate?.format(DATE_FORMATTER) ?: return null
        return EpcExpiredInsetViewModel(
            expiryDate = formattedDate,
            linkUrl = GET_NEW_EPC_URL,
        )
    }

    override fun getSupplementarySections(propertyCompliance: PropertyCompliance): List<SummaryCardSupplementarySection> =
        buildList {
            if (propertyCompliance.shouldShowEpcTenancySection) {
                val tenancyAnswer =
                    if (propertyCompliance.tenancyStartedBeforeEpcExpiry == true) "commonText.yes" else "commonText.no"

                add(
                    SummaryCardSupplementarySection(
                        bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.epcHasExpired",
                        rows =
                            listOf(
                                SummaryListRowViewModel(
                                    fieldHeading = "propertyDetails.complianceInformation.energyPerformance.wasEpcValidWhenTenancyBegan",
                                    fieldValue = tenancyAnswer,
                                ),
                            ),
                    ),
                )
            }

            if (propertyCompliance.shouldShowEpcMeesSection) {
                add(
                    SummaryCardSupplementarySection(
                        bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.lowRatingText",
                        rows = getMeesExemptionRows(propertyCompliance),
                    ),
                )
            }
        }

    override fun fromEntity(propertyCompliance: PropertyCompliance): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                if (propertyCompliance.epcExemptionReason != null) {
                    addRow(
                        key = "propertyDetails.complianceInformation.energyPerformance.hasEpc",
                        value = "commonText.no",
                    )
                    addRow(
                        key = "propertyDetails.complianceInformation.energyPerformance.isEpcRequired",
                        value = "commonText.no",
                    )
                    addRow(
                        key = "propertyDetails.complianceInformation.energyPerformance.epcExemption",
                        value = MessageKeyConverter.convert(propertyCompliance.epcExemptionReason!!),
                    )
                    return@apply
                }

                val status = getStatus(propertyCompliance)

                if (!propertyCompliance.hasEpcUrl) {
                    addRow(
                        key = "propertyDetails.complianceInformation.energyPerformance.hasEpc",
                        value = getMissingCertValue(status, propertyCompliance),
                    )
                    if (propertyCompliance.propertyOwnership.isOccupied &&
                        status != ComplianceCertStatus.PROVIDE_LATER
                    ) {
                        addRow(
                            key = "propertyDetails.complianceInformation.energyPerformance.isEpcRequired",
                            value = "commonText.yes",
                        )
                    }
                    return@apply
                }

                if (!propertyCompliance.isEpcNonExpiredButLowRating) {
                    addRow(
                        key = "propertyDetails.complianceInformation.certificateStatus",
                        value =
                            if (status == ComplianceCertStatus.ADDED || propertyCompliance.isEpcValidDespiteExpiry) {
                                TagValue.VALID
                            } else {
                                TagValue.EXPIRED
                            },
                    )
                }

                addRow(
                    key = "propertyDetails.complianceInformation.energyPerformance.energyRating",
                    value = propertyCompliance.epcEnergyRating?.uppercase(),
                )
                addRow(
                    key = "propertyDetails.complianceInformation.energyPerformance.expiryDate",
                    value = propertyCompliance.epcExpiryDate,
                )
                addRow(
                    key = "propertyDetails.complianceInformation.energyPerformance.certificateNumber",
                    value = propertyCompliance.epcUrl!!.substringAfterLast("/"),
                )
            }.toList()

    private fun getMeesExemptionRows(propertyCompliance: PropertyCompliance): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    key = "propertyDetails.complianceInformation.energyPerformance.energyEfficiencyExemption",
                    value = MessageKeyConverter.convert(propertyCompliance.epcMeesExemptionReason != null),
                )
                if (propertyCompliance.epcMeesExemptionReason != null) {
                    addRow(
                        key = "propertyDetails.complianceInformation.energyPerformance.registeredExemption",
                        value = MessageKeyConverter.convert(propertyCompliance.epcMeesExemptionReason!!),
                    )
                }
            }.toList()
}
