package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordOtherLandlordLeftNotification

@PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlord-other-landlord-left-email-flag-on")
interface JointLandlordOtherLandlordLeftEmailService {
    fun sendNotificationToRemainingLandlords(
        propertyOwnership: PropertyOwnership,
        previousLandlord: Landlord,
    )
}

@Primary
@PrsdbWebService("joint-landlord-other-landlord-left-email-flag-off")
class JointLandlordOtherLandlordLeftEmailServiceImplFlagOff : JointLandlordOtherLandlordLeftEmailService {
    override fun sendNotificationToRemainingLandlords(
        propertyOwnership: PropertyOwnership,
        previousLandlord: Landlord,
    ) {
        // No-op: the joint-landlords feature is disabled.
    }
}

@PrsdbWebService("joint-landlord-other-landlord-left-email-flag-on")
class JointLandlordOtherLandlordLeftEmailServiceImplFlagOn(
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val otherLandlordLeftEmailService: EmailNotificationService<JointLandlordOtherLandlordLeftNotification>,
) : JointLandlordOtherLandlordLeftEmailService {
    override fun sendNotificationToRemainingLandlords(
        propertyOwnership: PropertyOwnership,
        previousLandlord: Landlord,
    ) {
        // TODO: PDJB-1274: Update emails to account for org landlord
        check(previousLandlord is IndividualLandlord)
        propertyOwnership.landlords.forEach { otherLandlord ->
            check(otherLandlord is IndividualLandlord)
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
