package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.security.access.prepost.PreAuthorize
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_ROLES

class AllowIfLandlordTests {
    @Test
    fun `AllowIfLandlord authorises exactly the roles in LANDLORD_ROLES`() {
        val preAuthorize = AllowIfLandlord::class.java.getAnnotation(PreAuthorize::class.java)
        assertNotNull(preAuthorize)

        val expectedRoleNames = LANDLORD_ROLES.map { it.removePrefix("ROLE_") }
        val expectedExpression = "hasAnyRole(${expectedRoleNames.joinToString(", ") { "'$it'" }})"

        assertEquals(expectedExpression, preAuthorize.value)
    }
}
