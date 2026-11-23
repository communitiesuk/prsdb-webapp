package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordOtherLandlordLeftNotification

@PrsdbWebService
class JointLandlordOtherLandlordLeftEmailService(
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val otherLandlordLeftEmailService: EmailNotificationService<JointLandlordOtherLandlordLeftNotification>,
    private val featureFlagManager: FeatureFlagManager,
) {
    fun sendNotificationToRemainingLandlords(
        propertyOwnership: PropertyOwnership,
        previousLandlord: Landlord,
    ) {
        // TODO PDJB-2149: Remove the feature flag check when the ACTIVE_INACTIVE flag is removed
        val isActiveInactiveEnabled = featureFlagManager.checkFeature(ACTIVE_INACTIVE)
        propertyOwnership.landlords.forEach { otherLandlord ->
            otherLandlordLeftEmailService.sendEmailToLandlord(
                otherLandlord,
                JointLandlordOtherLandlordLeftNotification(
                    leavingLandlord = previousLandlord.name,
                    notifiedLandlord = otherLandlord.name,
                    address = propertyOwnership.address.toMultiLineAddress(),
                    propertyRecordUrl = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id).toString(),
                    useBeforePdjb844Template = !isActiveInactiveEnabled,
                ),
            )
        }
    }
}
