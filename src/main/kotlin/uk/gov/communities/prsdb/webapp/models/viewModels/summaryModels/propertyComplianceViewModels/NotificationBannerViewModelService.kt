package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationBannerLink
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationMessage

@PrsdbWebService
class NotificationBannerViewModelService {
    fun getNotificationMessageKeys(
        propertyCompliance: PropertyCompliance,
        isLandlordView: Boolean,
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
                    val viewSegment = if (isLandlordView) "landlord" else "localCouncil"
                    "$NOTIFICATION_KEY_PREFIX.missing.$viewSegment.mainText" to VIEW_COMPLIANCE_CERTIFICATES_KEY
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

    fun getIsAllValid(propertyCompliance: PropertyCompliance): Boolean =
        ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).isAllValid

    companion object {
        private const val NOTIFICATION_KEY_PREFIX = "propertyDetails.complianceInformation.notificationBanner"
        private const val VIEW_COMPLIANCE_CERTIFICATES_KEY = "$NOTIFICATION_KEY_PREFIX.viewComplianceCertificates"
    }
}
