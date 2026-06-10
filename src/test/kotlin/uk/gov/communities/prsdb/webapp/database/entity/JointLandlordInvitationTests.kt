package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.JointLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import java.time.Instant
import java.time.temporal.ChronoUnit

class JointLandlordInvitationTests {
    @Test
    fun `status returns PENDING when the current day is earlier than the expiry date`() {
        val createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS - 1).toLong(), ChronoUnit.DAYS)
        val invitation = MockJointLandlordData.createJointLandlordInvitation(createdDate = createdDate)

        assertEquals(invitation.status, JointLandlordInvitationStatus.PENDING)
    }

    @Test
    fun `status returns PENDING when the current day equals the expiry date`() {
        val createdDate = Instant.now().minus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS.toLong(), ChronoUnit.DAYS)
        val invitation = MockJointLandlordData.createJointLandlordInvitation(createdDate = createdDate)

        assertEquals(invitation.status, JointLandlordInvitationStatus.PENDING)
    }

    @Test
    fun `status returns EXPIRED when the current day is later than the expiry date`() {
        val createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(), ChronoUnit.DAYS)
        val invitation = MockJointLandlordData.createJointLandlordInvitation(createdDate = createdDate)

        assertEquals(invitation.status, JointLandlordInvitationStatus.EXPIRED)
    }
}
