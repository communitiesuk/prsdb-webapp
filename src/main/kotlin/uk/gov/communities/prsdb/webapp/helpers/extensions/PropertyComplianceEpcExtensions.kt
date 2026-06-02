package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.constants.EPC_ACCEPTABLE_RATING_RANGE
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

val PropertyCompliance.isEpcEnergyRatingLow: Boolean
    get() = epcEnergyRating?.uppercase()?.let { it !in EPC_ACCEPTABLE_RATING_RANGE } ?: false

val PropertyCompliance.isEpcNonExpiredButLowRating: Boolean
    get() = isEpcExpired != true && isEpcRatingLow == true

val PropertyCompliance.shouldShowCouncilWillSeeEpcInset: Boolean
    get() =
        propertyOwnership.isOccupied &&
            (
                (!hasEpcUrl && !hasEpcExemption && epcProvideLater != true) ||
                    (isEpcExpired == true && tenancyStartedBeforeEpcExpiry == false) ||
                    (isEpcRatingLow == true)
            )

val PropertyCompliance.shouldShowEpcExpiredNaturallyInset: Boolean
    get() =
        isEpcExpired == true &&
            tenancyStartedBeforeEpcExpiry == null &&
            propertyOwnership.isOccupied &&
            isEpcRatingLow != true

val PropertyCompliance.shouldShowEpcTenancySection: Boolean
    get() = isEpcExpired == true && tenancyStartedBeforeEpcExpiry != null

val PropertyCompliance.shouldShowEpcMeesSection: Boolean
    get() =
        (epcMeesExemptionReason != null || isEpcEnergyRatingLow) &&
            tenancyStartedBeforeEpcExpiry != false
