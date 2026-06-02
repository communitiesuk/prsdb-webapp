package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.constants.EPC_ACCEPTABLE_RATING_RANGE
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

val PropertyCompliance.isEpcEnergyRatingLow: Boolean
    get() = epcEnergyRating?.uppercase()?.let { it !in EPC_ACCEPTABLE_RATING_RANGE } ?: false

val PropertyCompliance.isEpcNonExpiredButLowRating: Boolean
    get() = isEpcExpired != true && isEpcRatingLow == true

private val PropertyCompliance.hasNoEpcAndNotProvidingLater: Boolean
    get() = !hasEpcUrl && !hasEpcExemption && epcProvideLater != true

private val PropertyCompliance.isEpcExpiredAfterTenancyStart: Boolean
    get() = isEpcExpired == true && tenancyStartedBeforeEpcExpiry == false

val PropertyCompliance.shouldShowCouncilWillSeeEpcInset: Boolean
    get() =
        propertyOwnership.isOccupied &&
            (
                hasNoEpcAndNotProvidingLater ||
                    isEpcExpiredAfterTenancyStart ||
                    isEpcRatingLow == true
            )

private val PropertyCompliance.didEpcBecomeExpired: Boolean
    get() = isEpcExpired == true && tenancyStartedBeforeEpcExpiry == null

val PropertyCompliance.shouldShowEpcBecameExpiredInset: Boolean
    get() =
        propertyOwnership.isOccupied &&
            didEpcBecomeExpired &&
            isEpcRatingLow != true

val PropertyCompliance.shouldShowEpcTenancySection: Boolean
    get() = isEpcExpired == true && tenancyStartedBeforeEpcExpiry != null

val PropertyCompliance.shouldShowEpcMeesSection: Boolean
    get() =
        (epcMeesExemptionReason != null || isEpcEnergyRatingLow) &&
            tenancyStartedBeforeEpcExpiry != false

val PropertyCompliance.isEpcValidDespiteExpiry: Boolean
    get() = tenancyStartedBeforeEpcExpiry == true && isEpcRatingLow != true
