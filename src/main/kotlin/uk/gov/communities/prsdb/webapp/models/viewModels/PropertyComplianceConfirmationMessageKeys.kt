package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

class PropertyComplianceConfirmationMessageKeys(
    private val propertyCompliance: PropertyCompliance,
) {
    val nonCompliantMsgKeys = listOfNotNull(nonCompliantGasSafetyMsgKey, nonCompliantElectricalSafetyMsgKey, nonCompliantEpcMsgKey)

    val compliantMsgKeys =
        listOfNotNull(
            compliantGasSafetyMsgKey,
            compliantElectricalSafetyMsgKey,
            compliantEpcMsgKey,
            compliantLandlordResponsibilitiesMsgKey,
        )

    private val electricalSafetyCertKeyPrefix get() =
        when (propertyCompliance.electricalCertType) {
            CertificateType.Eic -> "electricalSafety.eic"
            CertificateType.Eicr -> "electricalSafety.eicr"
            else -> "electricalSafety"
        }

    val nonCompliantGasSafetyMsgKey get() =
        when {
            propertyCompliance.isGasSafetyCertExpired == true -> "propertyCompliance.confirmation.nonCompliant.bullet.gasSafety.expired"
            propertyCompliance.isGasSafetyCertMissing -> "propertyCompliance.confirmation.nonCompliant.bullet.gasSafety.missing"
            else -> null
        }

    private val compliantGasSafetyMsgKey get() =
        if (nonCompliantGasSafetyMsgKey == null) {
            "propertyCompliance.confirmation.compliant.bullet.gasSafety"
        } else {
            null
        }

    val nonCompliantElectricalSafetyMsgKey get() =
        when {
            propertyCompliance.isElectricalSafetyExpired == true -> {
                "propertyCompliance.confirmation.nonCompliant.bullet.$electricalSafetyCertKeyPrefix.expired"
            }

            propertyCompliance.isElectricalSafetyMissing -> {
                "propertyCompliance.confirmation.nonCompliant.bullet.$electricalSafetyCertKeyPrefix.missing"
            }

            else -> {
                null
            }
        }

    private val compliantElectricalSafetyMsgKey get() =
        if (nonCompliantElectricalSafetyMsgKey == null) {
            "propertyCompliance.confirmation.compliant.bullet.electricalSafety"
        } else {
            null
        }

    val nonCompliantEpcMsgKey get() =
        when {
            propertyCompliance.isEpcNonCompliantDueToExpiry == true && propertyCompliance.isEpcRatingLow == true -> {
                "propertyCompliance.confirmation.nonCompliant.bullet.epc.expiredAndLowRating"
            }

            propertyCompliance.isEpcNonCompliantDueToExpiry == true -> {
                "propertyCompliance.confirmation.nonCompliant.bullet.epc.expired"
            }

            propertyCompliance.isEpcRatingLow == true -> {
                "propertyCompliance.confirmation.nonCompliant.bullet.epc.lowRating"
            }

            propertyCompliance.isEpcMissing -> {
                "propertyCompliance.confirmation.nonCompliant.bullet.epc.missing"
            }

            else -> {
                null
            }
        }

    private val compliantEpcMsgKey get() =
        if (nonCompliantEpcMsgKey == null) {
            "propertyCompliance.confirmation.compliant.bullet.epc"
        } else {
            null
        }

    private val compliantLandlordResponsibilitiesMsgKey get() =
        "propertyCompliance.confirmation.compliant.bullet.responsibilities"
}
