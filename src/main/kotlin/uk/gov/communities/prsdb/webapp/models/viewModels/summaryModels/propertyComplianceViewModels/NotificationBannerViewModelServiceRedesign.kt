package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel

@PrsdbWebService("notificationBannerViewModelServiceRedesign")
class NotificationBannerViewModelServiceRedesign : NotificationBannerViewModelService {
    override fun getNotificationMessageKeys(
        propertyCompliance: PropertyCompliance,
    ): List<PropertyComplianceViewModel.PropertyComplianceNotificationMessage> {
        val statusModel = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance)

        val isGasExpired = statusModel.gasSafetyStatus == ComplianceCertStatus.EXPIRED
        val isElectricalExpired = statusModel.eicrStatus == ComplianceCertStatus.EXPIRED
        val isEpcExpired = statusModel.epcStatus == ComplianceCertStatus.EXPIRED
        val displayAnyExpired = isGasExpired || isElectricalExpired || isEpcExpired
        val expiredCerts = listOf(isGasExpired, isElectricalExpired, isEpcExpired).count { it }

        val missingStatuses = listOf(ComplianceCertStatus.NOT_ADDED, ComplianceCertStatus.PROVIDE_LATER)
        val isOccupied = statusModel.isOccupied
        val displayIsGasMissing = isOccupied && statusModel.gasSafetyStatus in missingStatuses
        val displayIsElectricalMissing = isOccupied && statusModel.eicrStatus in missingStatuses
        val displayIsEpcMissing = isOccupied && statusModel.epcStatus in missingStatuses

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

    override fun getIsAllValid(propertyCompliance: PropertyCompliance): Boolean {
        val statusModel = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance)
        val validStatuses = listOf(ComplianceCertStatus.ADDED, ComplianceCertStatus.NOT_REQUIRED)
        return statusModel.gasSafetyStatus in validStatuses &&
            statusModel.eicrStatus in validStatuses &&
            statusModel.epcStatus in validStatuses
    }

    companion object {
        private const val NOTIFICATION_KEY_PREFIX = "propertyDetails.complianceInformation.notificationBanner"
    }
}
