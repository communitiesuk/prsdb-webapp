package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.enums.JointLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository

@PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "swap-to-individual-nudge-email-flag-on")
interface SwapToIndividualNudgeEmailService {
    fun sendNudgeEmailIfApplicable(propertyOwnership: PropertyOwnership)
}

@Primary
@Service("swap-to-individual-nudge-email-flag-off")
class SwapToIndividualNudgeEmailServiceImplFlagOff : SwapToIndividualNudgeEmailService {
    override fun sendNudgeEmailIfApplicable(propertyOwnership: PropertyOwnership) {
        // No-op: the joint-landlords feature is disabled.
    }
}

@Service("swap-to-individual-nudge-email-flag-on")
class SwapToIndividualNudgeEmailServiceImplFlagOn(
    private val invitationRepository: JointLandlordInvitationRepository,
) : SwapToIndividualNudgeEmailService {
    override fun sendNudgeEmailIfApplicable(propertyOwnership: PropertyOwnership) {
        if (!propertyOwnership.markedJointLandlord) return
        if (propertyOwnership.landlords.size != 1) return

        val hasPendingInvitations =
            invitationRepository
                .findByRegisteredOwnership(propertyOwnership)
                .any { it.status == JointLandlordInvitationStatus.PENDING }
        if (hasPendingInvitations) return

        // TODO: Send the nudge email once the template and Notify integration are finalised
    }
}
