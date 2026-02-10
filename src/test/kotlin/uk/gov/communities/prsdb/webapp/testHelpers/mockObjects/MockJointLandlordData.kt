package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import java.time.Instant
import java.util.UUID

class MockJointLandlordData {
    companion object {
        const val DEFAULT_JOINT_LANDLORD_INVITATION_ID = 123L

        fun createJointLandlordInvitation(
            id: Long = DEFAULT_JOINT_LANDLORD_INVITATION_ID,
            token: UUID = UUID.randomUUID(),
            email: String = "joint.landlord@example.com",
            propertyOwnership: PropertyOwnership = MockLandlordData.createPropertyOwnership(),
            createdDate: Instant = Instant.now(),
        ): JointLandlordInvitation {
            val jointLandlordInvitation =
                JointLandlordInvitation(
                    id = id,
                    token = token,
                    email = email,
                    registeredPropertyID = propertyOwnership,
                )

            ReflectionTestUtils.setField(jointLandlordInvitation, "createdDate", createdDate)

            return jointLandlordInvitation
        }
    }
}
