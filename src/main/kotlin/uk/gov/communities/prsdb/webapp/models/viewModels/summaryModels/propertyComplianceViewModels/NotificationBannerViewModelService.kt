package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationBannerLink
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationMessage

@PrsdbWebService
class NotificationBannerViewModelService {
    fun getPropertyDetailsNotificationBanner(
        propertyCompliance: PropertyCompliance,
        isLandlordView: Boolean,
        isOccupied: Boolean,
        isLicensingProvideLater: Boolean,
        isTenancyProvideLater: Boolean,
    ): PropertyDetailsNotificationBannerViewModel {
        val complianceMessages = getComplianceNotificationMessageKeys(propertyCompliance, isLandlordView)

        return PropertyDetailsNotificationBannerViewModel.fromState(
            isLandlordView = isLandlordView,
            isOccupied = isOccupied,
            isLicensingProvideLater = isLicensingProvideLater,
            isTenancyProvideLater = isTenancyProvideLater,
            complianceMessages = complianceMessages,
        )
    }

    fun getComplianceNotificationMessageKeys(
        propertyCompliance: PropertyCompliance,
        isLandlordView: Boolean,
    ): List<NotificationMessage> {
        val statusModel = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance)

        val isGasExpired = statusModel.gasSafetyStatus == ComplianceCertStatus.EXPIRED
        val isElectricalExpired = statusModel.electricalSafetyStatus == ComplianceCertStatus.EXPIRED
        val isEpcExpired = statusModel.epcStatus == ComplianceCertStatus.EXPIRED

        val (mainTextKey, specificLinkTextKey) =
            when {
                statusModel.displayAnyMissingOrFaulty && statusModel.expiredCertificateCount > 0 -> {
                    "$NOTIFICATION_KEY_PREFIX.missingAndExpired.mainText" to VIEW_COMPLIANCE_CERTIFICATES_KEY
                }

                statusModel.displayAnyMissingOrFaulty -> {
                    missingMainTextKey(isLandlordView) to VIEW_COMPLIANCE_CERTIFICATES_KEY
                }

                statusModel.expiredCertificateCount > 1 -> {
                    "$NOTIFICATION_KEY_PREFIX.multipleExpired.mainText" to VIEW_COMPLIANCE_CERTIFICATES_KEY
                }

                isGasExpired -> {
                    "$NOTIFICATION_KEY_PREFIX.gasCert.expired.mainText" to "$NOTIFICATION_KEY_PREFIX.gasCert.expired.linkText"
                }

                isElectricalExpired -> {
                    "$NOTIFICATION_KEY_PREFIX.electricalCert.expired.mainText" to
                        "$NOTIFICATION_KEY_PREFIX.electricalCert.expired.linkText"
                }

                isEpcExpired -> {
                    "$NOTIFICATION_KEY_PREFIX.epc.expired.mainText" to "$NOTIFICATION_KEY_PREFIX.epc.expired.linkText"
                }

                else -> {
                    return emptyList()
                }
            }

        return listOf(
            NotificationMessage(
                mainText = mainTextKey,
                links =
                    listOf(
                        NotificationBannerLink(
                            linkUrl = "#$COMPLIANCE_INFO_FRAGMENT",
                            linkText = specificLinkTextKey,
                            afterLinkText = "$NOTIFICATION_KEY_PREFIX.afterLinkText",
                        ),
                    ),
            ),
        )
    }

    private fun missingMainTextKey(isLandlordView: Boolean): String =
        when {
            isLandlordView -> "$NOTIFICATION_KEY_PREFIX.missing.landlord.mainText"
            else -> "$NOTIFICATION_KEY_PREFIX.missing.localCouncil.mainText"
        }

    companion object {
        private const val NOTIFICATION_KEY_PREFIX = "propertyDetails.complianceInformation.notificationBanner"
        private const val VIEW_COMPLIANCE_CERTIFICATES_KEY = "$NOTIFICATION_KEY_PREFIX.viewComplianceCertificates"
    }
}
