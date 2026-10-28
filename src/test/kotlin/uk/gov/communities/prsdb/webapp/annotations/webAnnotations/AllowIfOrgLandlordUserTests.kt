package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.security.access.prepost.PreAuthorize
import uk.gov.communities.prsdb.webapp.constants.ORG_LANDLORD_ROLES

class AllowIfOrgLandlordUserTests {
    @Test
    fun `AllowIfOrgLandlordUser authorises exactly the roles in ORG_LANDLORD_ROLES`() {
        val preAuthorize = AllowIfOrgLandlordUser::class.java.getAnnotation(PreAuthorize::class.java)
        assertNotNull(preAuthorize)

        val expectedRoleNames = ORG_LANDLORD_ROLES.map { it.removePrefix("ROLE_") }
        val expectedExpression = "hasAnyRole(${expectedRoleNames.joinToString(", ") { "'$it'" }})"

        assertEquals(expectedExpression, preAuthorize.value)
    }
}
