package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.EPC_ACCEPTABLE_RATING_RANGE
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardSupplementarySection
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.TagValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@PrsdbWebService("epcViewModelServiceRedesign")
class EpcViewModelFactory(
    private val messageSource: MessageSource,
) : EpcViewModelService {
    override fun getInsetTextKey(propertyCompliance: PropertyCompliance): String? {
        val status = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).epcStatus

        return when {
            status == ComplianceCertStatus.NOT_ADDED &&
                propertyCompliance.propertyOwnership.isOccupied -> {
                "propertyCompliance.epcTask.checkEpcAnswers.occupiedNoEpcInset"
            }

            propertyCompliance.isEpcExpired == true &&
                propertyCompliance.propertyOwnership.isOccupied &&
                propertyCompliance.tenancyStartedBeforeEpcExpiry == false -> {
                "propertyCompliance.epcTask.checkEpcAnswers.occupiedNoEpcInset"
            }

            propertyCompliance.isEpcRatingLow == true &&
                propertyCompliance.propertyOwnership.isOccupied &&
                (
                    propertyCompliance.isEpcExpired != true ||
                        propertyCompliance.tenancyStartedBeforeEpcExpiry != null
                ) -> {
                "propertyCompliance.epcTask.checkEpcAnswers.occupiedNoEpcInset"
            }

            else -> {
                null
            }
        }
    }

    override fun getInsetTextHtml(propertyCompliance: PropertyCompliance): String? {
        if (propertyCompliance.isEpcExpired != true) return null
        if (propertyCompliance.tenancyStartedBeforeEpcExpiry != null) return null
        if (!propertyCompliance.propertyOwnership.isOccupied) return null

        val expiryDate = propertyCompliance.epcExpiryDate ?: return null
        val formattedDate = expiryDate.format(DATE_FORMATTER)
        return messageSource.getMessageForKey(
            NATURALLY_EXPIRED_INSET_KEY,
            arrayOf(formattedDate, GET_NEW_EPC_URL),
        )
    }

    override fun getSupplementarySections(propertyCompliance: PropertyCompliance): List<SummaryCardSupplementarySection> {
        val sections = mutableListOf<SummaryCardSupplementarySection>()

        if (propertyCompliance.isEpcExpired == true && propertyCompliance.tenancyStartedBeforeEpcExpiry != null) {
            val tenancyAnswer =
                if (propertyCompliance.tenancyStartedBeforeEpcExpiry == true) "commonText.yes" else "commonText.no"

            sections.add(
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

        val showMeesSection =
            shouldAddMeesExemptionRow(propertyCompliance) &&
                propertyCompliance.tenancyStartedBeforeEpcExpiry != false

        if (showMeesSection) {
            sections.add(
                SummaryCardSupplementarySection(
                    bodyTextKey = "propertyDetails.complianceInformation.energyPerformance.lowRatingText",
                    rows = getMeesExemptionRows(propertyCompliance),
                ),
            )
        }

        return sections
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
                        key = "propertyCompliance.epcTask.checkEpcAnswers.isEpcRequired",
                        value = "commonText.no",
                    )
                    addRow(
                        key = "propertyCompliance.epcTask.checkEpcAnswers.epcExemption",
                        value = MessageKeyConverter.convert(propertyCompliance.epcExemptionReason!!),
                    )
                    return@apply
                }

                val status = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).epcStatus

                if (propertyCompliance.epcUrl == null) {
                    addRow(
                        key = "propertyDetails.complianceInformation.energyPerformance.hasEpc",
                        value = getMissingCertValue(status, propertyCompliance),
                    )
                    if (propertyCompliance.propertyOwnership.isOccupied &&
                        status != ComplianceCertStatus.PROVIDE_LATER
                    ) {
                        addRow(
                            key = "propertyCompliance.epcTask.checkEpcAnswers.isEpcRequired",
                            value = "commonText.yes",
                        )
                    }
                    return@apply
                }

                val isValidDespiteExpiry =
                    propertyCompliance.tenancyStartedBeforeEpcExpiry == true &&
                        propertyCompliance.isEpcRatingLow != true
                val hasValidCertificate = status == ComplianceCertStatus.ADDED || isValidDespiteExpiry
                val isNonExpiredButLowRating =
                    propertyCompliance.isEpcExpired != true && propertyCompliance.isEpcRatingLow == true

                if (!isNonExpiredButLowRating) {
                    addRow(
                        key = "propertyDetails.complianceInformation.certificateStatus",
                        value =
                            if (hasValidCertificate) {
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

    private fun getMissingCertValue(
        status: ComplianceCertStatus,
        propertyCompliance: PropertyCompliance,
    ): Any {
        val isOccupied = propertyCompliance.propertyOwnership.isOccupied

        return when {
            !isOccupied -> "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied"
            status == ComplianceCertStatus.PROVIDE_LATER ->
                getProvideLaterWithDeadlineText(propertyCompliance.propertyOwnership.lastOccupiedDate)
            else -> "commonText.no"
        }
    }

    private fun getProvideLaterWithDeadlineText(lastOccupiedDate: LocalDate?): String {
        val deadline =
            lastOccupiedDate?.plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
                ?: throw IllegalStateException("Cannot get provide-later-with-deadline text without an occupied date")
        val formattedDate = deadline.format(DATE_FORMATTER)
        return messageSource.getMessageForKey(PROVIDE_LATER_WITH_DEADLINE_KEY, arrayOf(formattedDate))
    }

    private fun shouldAddMeesExemptionRow(propertyCompliance: PropertyCompliance): Boolean =
        propertyCompliance.epcMeesExemptionReason != null ||
            (
                propertyCompliance.epcEnergyRating != null &&
                    propertyCompliance.epcEnergyRating!!.uppercase() !in EPC_ACCEPTABLE_RATING_RANGE
            )

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
        private const val PROVIDE_LATER_WITH_DEADLINE_KEY =
            "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.occupiedWithDeadline"
        private const val NATURALLY_EXPIRED_INSET_KEY =
            "propertyDetails.complianceInformation.energyPerformance.epcExpiredNaturallyInset"
    }
}
