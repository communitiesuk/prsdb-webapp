package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsBeforePdjb939NotificationBannerViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationBannerLink
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationMessage

@PrsdbWebService
class NotificationBannerViewModelService {
    // Assembles the unified property-record banner (flag on), merging compliance messages with the
    // provide-later registration state. A null propertyCompliance means no certificates exist yet.
    fun getPropertyDetailsNotificationBanner(
        propertyCompliance: PropertyCompliance?,
        isLandlordView: Boolean,
        isOccupied: Boolean,
        isLicensingProvideLater: Boolean,
        isTenancyProvideLater: Boolean,
    ): PropertyDetailsNotificationBannerViewModel {
        val complianceMessages =
            propertyCompliance?.let { getComplianceNotificationMessageKeys(it, isLandlordView) }
                ?: listOf(NotificationMessage(mainText = noComplianceMessageKey(isLandlordView)))

        return PropertyDetailsNotificationBannerViewModel.fromState(
            isLandlordView = isLandlordView,
            isOccupied = isOccupied,
            isLicensingProvideLater = isLicensingProvideLater,
            isTenancyProvideLater = isTenancyProvideLater,
            complianceMessages = complianceMessages,
        )
    }

    fun getBeforePdjb939NotificationBanner(
        propertyCompliance: PropertyCompliance?,
        isLandlordView: Boolean,
    ): PropertyDetailsBeforePdjb939NotificationBannerViewModel {
        // The flag-off banner is compliance-only and only shown to landlords.
        val complianceMessages =
            propertyCompliance?.let {
                if (isLandlordView) getComplianceNotificationMessageKeys(it, isLandlordView, beforePdjb939 = true) else emptyList()
            }

        return PropertyDetailsBeforePdjb939NotificationBannerViewModel.fromState(isLandlordView, complianceMessages)
    }

    fun getComplianceNotificationMessageKeys(
        propertyCompliance: PropertyCompliance,
        isLandlordView: Boolean,
        beforePdjb939: Boolean = false,
    ): List<NotificationMessage> {
        val statusModel = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance)

        val isGasExpired = statusModel.gasSafetyStatus == ComplianceCertStatus.EXPIRED
        val isElectricalExpired = statusModel.electricalSafetyStatus == ComplianceCertStatus.EXPIRED
        val isEpcExpired = statusModel.epcStatus == ComplianceCertStatus.EXPIRED

        val (mainTextKey, linkTextKey) =
            when {
                statusModel.displayAnyMissingOrFaulty && statusModel.expiredCertificateCount > 0 -> {
                    "$NOTIFICATION_KEY_PREFIX.missingAndExpired.mainText" to VIEW_COMPLIANCE_CERTIFICATES_KEY
                }

                statusModel.displayAnyMissingOrFaulty -> {
                    missingMainTextKey(isLandlordView, beforePdjb939) to VIEW_COMPLIANCE_CERTIFICATES_KEY
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
                            linkText = linkTextKey,
                            afterLinkText = "$NOTIFICATION_KEY_PREFIX.afterLinkText",
                        ),
                    ),
            ),
        )
    }

    // The flag-on banner uses view-specific "missing" copy; the flag-off (beforePdjb939) banner uses a single variant.
    private fun missingMainTextKey(
        isLandlordView: Boolean,
        beforePdjb939: Boolean,
    ): String =
        when {
            beforePdjb939 -> "$NOTIFICATION_KEY_PREFIX.missing.beforePdjb939.mainText"
            isLandlordView -> "$NOTIFICATION_KEY_PREFIX.missing.landlord.mainText"
            else -> "$NOTIFICATION_KEY_PREFIX.missing.localCouncil.mainText"
        }

    private fun noComplianceMessageKey(isLandlordView: Boolean): String =
        if (isLandlordView) {
            "propertyDetails.complianceInformation.noCompliance.landlordView.mainText"
        } else {
            "propertyDetails.complianceInformation.noCompliance.localCouncilView.mainText"
        }

    companion object {
        private const val NOTIFICATION_KEY_PREFIX = "propertyDetails.complianceInformation.notificationBanner"
        private const val VIEW_COMPLIANCE_CERTIFICATES_KEY = "$NOTIFICATION_KEY_PREFIX.viewComplianceCertificates"
    }
}
