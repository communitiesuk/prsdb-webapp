package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import java.time.Instant
import java.time.temporal.ChronoUnit

class JointLandlordInvitationTests {
    @Test
    fun `isExpired returns false when the current day is earlier than the expiry date`() {
        val createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS - 1).toLong(), ChronoUnit.DAYS)
        val invitation = MockJointLandlordData.createJointLandlordInvitation(createdDate = createdDate)

        assertFalse(invitation.isExpired)
    }

    @Test
    fun `isExpired returns false when the current day equals the expiry date`() {
        val createdDate = Instant.now().minus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS.toLong(), ChronoUnit.DAYS)
        val invitation = MockJointLandlordData.createJointLandlordInvitation(createdDate = createdDate)

        assertFalse(invitation.isExpired)
    }

    @Test
    fun `isExpired returns true when the current day is later than the expiry date`() {
        val createdDate = Instant.now().minus((JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(), ChronoUnit.DAYS)
        val invitation = MockJointLandlordData.createJointLandlordInvitation(createdDate = createdDate)

        assertTrue(invitation.isExpired)
    }
}
