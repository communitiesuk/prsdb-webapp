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
        val isElectricalExpired = statusModel.electricalSafetyStatus == ComplianceCertStatus.EXPIRED
        val isEpcExpired = statusModel.epcStatusMay2026Redesign == ComplianceCertStatus.EXPIRED

        val mainTextKey =
            when {
                statusModel.displayAnyMissingOrFaulty && statusModel.expiredCertificateCount > 0 -> {
                    "$NOTIFICATION_KEY_PREFIX.missingAndExpired.mainText"
                }

                statusModel.displayAnyMissingOrFaulty -> {
                    "$NOTIFICATION_KEY_PREFIX.missing.mainText"
                }

                statusModel.expiredCertificateCount > 1 -> {
                    "$NOTIFICATION_KEY_PREFIX.multipleExpired.mainText"
                }

                isGasExpired -> {
                    "$NOTIFICATION_KEY_PREFIX.gasCert.expired.mainText"
                }

                isElectricalExpired -> {
                    "$NOTIFICATION_KEY_PREFIX.electricalCert.expired.mainText"
                }

                isEpcExpired -> {
                    "$NOTIFICATION_KEY_PREFIX.epc.expired.mainText"
                }

                else -> {
                    return emptyList()
                }
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
        ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).isAllValid

    companion object {
        private const val NOTIFICATION_KEY_PREFIX = "propertyDetails.complianceInformation.notificationBanner"
    }
}
