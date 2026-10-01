package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.security.access.prepost.PreAuthorize

class AllowIfAnyLandlordTests {
    @Test
    fun `AllowIfAnyLandlord is meta-annotated with a PreAuthorize permitting every landlord-domain role`() {
        val preAuthorize = AllowIfAnyLandlord::class.java.getAnnotation(PreAuthorize::class.java)

        assertNotNull(preAuthorize)
        assertEquals("hasAnyRole('INDIVIDUAL_LANDLORD', 'ORG_ADMIN', 'ORG_EDITOR')", preAuthorize.value)
    }
}
