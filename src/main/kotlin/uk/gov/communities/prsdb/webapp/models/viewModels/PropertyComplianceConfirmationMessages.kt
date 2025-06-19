package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

class PropertyComplianceConfirmationMessages(
    private val propertyCompliance: PropertyCompliance,
) {
    val nonCompliantMsgs = listOfNotNull(nonCompliantGasSafetyMsg, nonCompliantEicrMsg, nonCompliantEpcMsg)

    val compliantMsgs = listOfNotNull(compliantGasSafetyMsg, compliantEicrMsg, compliantEpcMsg, compliantLandlordResponsibilitiesMsg)

    private val nonCompliantGasSafetyMsg get() =
        when {
            propertyCompliance.isGasSafetyCertExpired == true -> "propertyCompliance.confirmation.nonCompliant.bullet.gasSafety.expired"
            propertyCompliance.isGasSafetyCertMissing -> "propertyCompliance.confirmation.nonCompliant.bullet.gasSafety.missing"
            else -> null
        }

    private val compliantGasSafetyMsg get() =
        if (nonCompliantGasSafetyMsg == null) {
            "propertyCompliance.confirmation.compliant.bullet.gasSafety"
        } else {
            null
        }

    private val nonCompliantEicrMsg get() =
        when {
            propertyCompliance.isEicrExpired == true -> "propertyCompliance.confirmation.nonCompliant.bullet.eicr.expired"
            propertyCompliance.isEicrMissing -> "propertyCompliance.confirmation.nonCompliant.bullet.eicr.missing"
            else -> null
        }

    private val compliantEicrMsg get() =
        if (nonCompliantEicrMsg == null) {
            "propertyCompliance.confirmation.compliant.bullet.eicr"
        } else {
            null
        }

    private val nonCompliantEpcMsg get() =
        when {
            propertyCompliance.isEpcExpiredAndLowRated() -> "propertyCompliance.confirmation.nonCompliant.bullet.epc.expiredAndLowRating"
            propertyCompliance.isEpcExpired == true -> "propertyCompliance.confirmation.nonCompliant.bullet.epc.expired"
            propertyCompliance.isEpcRatingLow == true -> "propertyCompliance.confirmation.nonCompliant.bullet.epc.lowRating"
            propertyCompliance.isEpcMissing -> "propertyCompliance.confirmation.nonCompliant.bullet.epc.missing"
            else -> null
        }

    private val compliantEpcMsg get() =
        if (nonCompliantEpcMsg == null) {
            "propertyCompliance.confirmation.compliant.bullet.epc"
        } else {
            null
        }

    private val compliantLandlordResponsibilitiesMsg get() =
        "propertyCompliance.confirmation.compliant.bullet.responsibilities"

    private fun PropertyCompliance.isEpcExpiredAndLowRated(): Boolean = isEpcExpired == true && isEpcRatingLow == true
}
