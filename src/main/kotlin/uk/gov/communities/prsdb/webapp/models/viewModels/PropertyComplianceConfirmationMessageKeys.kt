package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

class PropertyComplianceConfirmationMessageKeys(
    private val propertyCompliance: PropertyCompliance,
) {
    val nonCompliantMsgKeys = listOfNotNull(nonCompliantGasSafetyMsgKey, nonCompliantEicrMsgKey, nonCompliantEpcMsgKey)

    val compliantMsgKeys =
        listOfNotNull(compliantGasSafetyMsgKey, compliantEicrMsgKey, compliantEpcMsgKey, compliantLandlordResponsibilitiesMsgKey)

    private val nonCompliantGasSafetyMsgKey get() =
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

    private val nonCompliantEicrMsgKey get() =
        when {
            propertyCompliance.isEicrExpired == true -> "propertyCompliance.confirmation.nonCompliant.bullet.eicr.expired"
            propertyCompliance.isEicrMissing -> "propertyCompliance.confirmation.nonCompliant.bullet.eicr.missing"
            else -> null
        }

    private val compliantEicrMsgKey get() =
        if (nonCompliantEicrMsgKey == null) {
            "propertyCompliance.confirmation.compliant.bullet.eicr"
        } else {
            null
        }

    private val nonCompliantEpcMsgKey get() =
        when {
            propertyCompliance.isEpcExpiredAndLowRated() -> "propertyCompliance.confirmation.nonCompliant.bullet.epc.expiredAndLowRating"
            propertyCompliance.isEpcExpired == true -> "propertyCompliance.confirmation.nonCompliant.bullet.epc.expired"
            propertyCompliance.isEpcRatingLow == true -> "propertyCompliance.confirmation.nonCompliant.bullet.epc.lowRating"
            propertyCompliance.isEpcMissing -> "propertyCompliance.confirmation.nonCompliant.bullet.epc.missing"
            else -> null
        }

    private val compliantEpcMsgKey get() =
        if (nonCompliantEpcMsgKey == null) {
            "propertyCompliance.confirmation.compliant.bullet.epc"
        } else {
            null
        }

    private val compliantLandlordResponsibilitiesMsgKey get() =
        "propertyCompliance.confirmation.compliant.bullet.responsibilities"

    private fun PropertyCompliance.isEpcExpiredAndLowRated(): Boolean = isEpcExpired == true && isEpcRatingLow == true
}
