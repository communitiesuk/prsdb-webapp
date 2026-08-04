package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordOtherLandlordLeftNotification

@PrsdbWebService
class JointLandlordOtherLandlordLeftEmailService(
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val otherLandlordLeftEmailService: EmailNotificationService<JointLandlordOtherLandlordLeftNotification>,
) {
    fun sendNotificationToRemainingLandlords(
        propertyOwnership: PropertyOwnership,
        previousLandlord: Landlord,
    ) {
        // TODO: PDJB-1274: Update emails to account for org landlord
        propertyOwnership.landlords.forEach { otherLandlord ->
            otherLandlordLeftEmailService.sendEmail(
                otherLandlord.email,
                JointLandlordOtherLandlordLeftNotification(
                    leavingLandlord = previousLandlord.name,
                    notifiedLandlord = otherLandlord.name,
                    address = propertyOwnership.address.toMultiLineAddress(),
                    propertyRecordUrl = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id).toString(),
                ),
            )
        }
    }
}
