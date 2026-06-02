package uk.gov.communities.prsdb.webapp.helpers.extensions

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

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
        hasMeesRelevance &&
            tenancyStartedBeforeEpcExpiry != false
