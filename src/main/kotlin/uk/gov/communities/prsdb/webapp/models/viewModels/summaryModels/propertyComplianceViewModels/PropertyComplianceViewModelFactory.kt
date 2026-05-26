package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel

@PrsdbWebService
class PropertyComplianceViewModelFactory(
    private val gasSafetyViewModelFactory: GasSafetyViewModelFactory,
    private val electricalSafetyViewModelFactory: ElectricalSafetyViewModelFactory,
) {
    fun create(
        propertyCompliance: PropertyCompliance,
        landlordView: Boolean = true,
        propertyOwnershipId: Long,
    ): PropertyComplianceViewModel {
        val epcChangeActions =
            if (landlordView) {
                listOf(
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateEpcController.getUpdateEpcRouteFirstStep(propertyCompliance.propertyOwnership.id),
                    ),
                )
            } else {
                null
            }

        val electricalSafetyChangeActions =
            if (landlordView) {
                listOf(
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateElectricalSafetyController.getUpdateElectricalSafetyFirstStepRoute(propertyOwnershipId),
                    ),
                )
            } else {
                null
            }

        val gasSafetyChangeActions =
            if (landlordView) {
                listOf(
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateGasSafetyController.getUpdateGasSafetyFirstStepRoute(propertyOwnershipId),
                    ),
                )
            } else {
                null
            }

        val gasSafetySummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.gasSafety.heading",
                summaryList = gasSafetyViewModelFactory.fromEntity(propertyCompliance),
                actions = gasSafetyChangeActions,
            )

        val electricalSafetySummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.electricalSafety.heading",
                summaryList = electricalSafetyViewModelFactory.fromEntity(propertyCompliance),
                actions = electricalSafetyChangeActions,
            )

        val epcSummaryCard =
            SummaryCardViewModel(
                title = "propertyDetails.complianceInformation.energyPerformance.heading",
                summaryList = EpcViewModelBuilder.fromEntity(propertyCompliance),
                actions = epcChangeActions,
            )

        val notificationMessages = getNotificationMessageKeys(propertyCompliance)

        val isAllValid =
            !propertyCompliance.isGasSafetyCertMissing &&
                !propertyCompliance.isElectricalSafetyMissing &&
                !propertyCompliance.isEpcMissing &&
                propertyCompliance.isGasSafetyCertExpired != true &&
                propertyCompliance.isElectricalSafetyExpired != true &&
                propertyCompliance.isEpcExpired != true

        return PropertyComplianceViewModel(
            gasSafetySummaryCard = gasSafetySummaryCard,
            electricalSafetySummaryCard = electricalSafetySummaryCard,
            epcSummaryCard = epcSummaryCard,
            notificationMessages = notificationMessages,
            isAllValid = isAllValid,
        )
    }

    private fun getNotificationMessageKeys(
        propertyCompliance: PropertyCompliance,
    ): List<PropertyComplianceViewModel.PropertyComplianceNotificationMessage> {
        val isGasExpired = propertyCompliance.isGasSafetyCertExpired == true
        val isElectricalExpired = propertyCompliance.isElectricalSafetyExpired == true
        val isEpcExpired = propertyCompliance.isEpcExpired == true
        val displayAnyExpired = isGasExpired || isElectricalExpired || isEpcExpired
        val expiredCerts = listOf(isGasExpired, isElectricalExpired, isEpcExpired).count { it }

        val isOccupied = propertyCompliance.propertyOwnership.isOccupied
        val displayIsGasMissing = isOccupied && propertyCompliance.isGasSafetyCertMissing
        val displayIsElectricalMissing = isOccupied && propertyCompliance.isElectricalSafetyMissing
        val displayIsEpcMissing = isOccupied && propertyCompliance.isEpcMissing

        val displayAnyMissing = displayIsGasMissing || displayIsElectricalMissing || displayIsEpcMissing

        val mainTextKey =
            when {
                displayAnyMissing && displayAnyExpired -> "$NOTIFICATION_KEY_PREFIX.missingAndExpired.mainText"
                displayAnyMissing -> "$NOTIFICATION_KEY_PREFIX.missing.mainText"
                expiredCerts > 1 -> "$NOTIFICATION_KEY_PREFIX.multipleExpired.mainText"
                isGasExpired -> "$NOTIFICATION_KEY_PREFIX.gasCert.expired.mainText"
                isElectricalExpired -> "$NOTIFICATION_KEY_PREFIX.electricalCert.expired.mainText"
                isEpcExpired -> "$NOTIFICATION_KEY_PREFIX.epc.expired.mainText"
                else -> return emptyList()
            }

        return listOf(
            PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                mainText = mainTextKey,
                linkMessage =
                    PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                        linkUrl = "#$COMPLIANCE_INFO_FRAGMENT",
                        linkText = "$NOTIFICATION_KEY_PREFIX.viewComplianceCertificates",
                        afterLinkText = "$NOTIFICATION_KEY_PREFIX.afterLinkText",
                        isAfterLinkTextFullStop = true,
                    ),
            ),
        )
    }

    companion object {
        private const val NOTIFICATION_KEY_PREFIX = "propertyDetails.complianceInformation.notificationBanner"
    }
}
