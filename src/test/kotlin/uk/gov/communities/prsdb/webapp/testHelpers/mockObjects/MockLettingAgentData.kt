package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import java.time.Instant
import java.util.UUID

class MockLettingAgentData {
    companion object {
        const val DEFAULT_LETTING_AGENT_ACCESS_ID = 456L
        const val DEFAULT_ENCODED_PASSWORD = "\$argon2id\$v=19\$m=16384,t=2,p=1\$defaultsaltvalue1\$defaulthashvalue1234567890abcdefghij"

        fun createLettingAgentAccess(
            id: Long = DEFAULT_LETTING_AGENT_ACCESS_ID,
            token: UUID = UUID.randomUUID(),
            invitedEmail: String = "letting.agent@example.com",
            propertyOwnership: PropertyOwnership = MockLandlordData.createPropertyOwnership(),
            createdDate: Instant = Instant.now(),
            encodedPassword: String? = DEFAULT_ENCODED_PASSWORD,
        ): LettingAgentAccess {
            val lettingAgentAccess =
                LettingAgentAccess(
                    token = token,
                    invitedEmail = invitedEmail,
                    propertyOwnership = propertyOwnership,
                )

            ReflectionTestUtils.setField(lettingAgentAccess, "id", id)
            ReflectionTestUtils.setField(lettingAgentAccess, "createdDate", createdDate)
            if (encodedPassword != null) {
                ReflectionTestUtils.setField(lettingAgentAccess, "encodedPassword", encodedPassword)
            }

            return lettingAgentAccess
        }

        fun createLettingAgentAccessWithoutPassword(
            id: Long = DEFAULT_LETTING_AGENT_ACCESS_ID,
            token: UUID = UUID.randomUUID(),
            invitedEmail: String = "letting.agent@example.com",
            propertyOwnership: PropertyOwnership = MockLandlordData.createPropertyOwnership(),
            createdDate: Instant = Instant.now(),
        ): LettingAgentAccess = createLettingAgentAccess(id, token, invitedEmail, propertyOwnership, createdDate, encodedPassword = null)
    }
}
