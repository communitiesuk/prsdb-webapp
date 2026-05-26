package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

@PrsdbWebService("notificationBannerViewModelServiceRedesign")
class NotificationBannerViewModelFactory : NotificationBannerViewModelService {
    override fun getNotificationMessageKeys(
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

    override fun getIsAllValid(propertyCompliance: PropertyCompliance): Boolean =
        !propertyCompliance.isGasSafetyCertMissing &&
            !propertyCompliance.isElectricalSafetyMissing &&
            !propertyCompliance.isEpcMissing &&
            propertyCompliance.isGasSafetyCertExpired != true &&
            propertyCompliance.isElectricalSafetyExpired != true &&
            propertyCompliance.isEpcExpired != true

    companion object {
        private const val NOTIFICATION_KEY_PREFIX = "propertyDetails.complianceInformation.notificationBanner"
    }
}
