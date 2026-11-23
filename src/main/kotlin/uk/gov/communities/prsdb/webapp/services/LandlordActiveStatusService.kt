package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.database.entity.Landlord

@PrsdbWebService
class LandlordActiveStatusService(
    private val userToLandlordService: UserToLandlordService,
    // TODO PDJB-2149: Remove the feature flag manager when the ACTIVE_INACTIVE flag is removed
    private val featureFlagManager: FeatureFlagManager,
) {
    fun isCurrentLandlordInactive(): Boolean =
        userToLandlordService.getCurrentLandlordForUserOrNull()?.let { isLandlordInactive(it) } ?: false

    // TODO PDJB-2149: Remove the ACTIVE_INACTIVE check when the flag is removed
    fun isLandlordInactive(landlord: Landlord): Boolean = featureFlagManager.checkFeature(ACTIVE_INACTIVE) && !landlord.isActive
}
