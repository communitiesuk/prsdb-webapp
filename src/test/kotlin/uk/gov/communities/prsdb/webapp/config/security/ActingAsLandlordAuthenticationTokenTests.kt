package uk.gov.communities.prsdb.webapp.config.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ROLE_ACTING_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_ADMIN

class ActingAsLandlordAuthenticationTokenTests {
    @Test
    fun `token identifies as the acted as user with their landlord roles plus the acting marker`() {
        // Arrange, Act
        val token =
            ActingAsLandlordAuthenticationToken(
                actingAsUserId = "landlord-user-id",
                landlordRoles = listOf(ROLE_ORG_ADMIN),
                systemOperatorId = "system-operator-id",
            )

        // Assert
        assertEquals("landlord-user-id", token.name)
        assertEquals("landlord-user-id", token.principal)
        assertEquals(setOf(ROLE_ORG_ADMIN, ROLE_ACTING_AS_LANDLORD), token.authorities.map { it.authority }.toSet())
        assertTrue(token.isAuthenticated)
        assertEquals("system-operator-id", token.systemOperatorId)
    }
}
