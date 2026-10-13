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
        propertyOwnership.landlords.forEach { otherLandlord ->
            otherLandlordLeftEmailService.sendEmailToLandlord(
                otherLandlord,
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
