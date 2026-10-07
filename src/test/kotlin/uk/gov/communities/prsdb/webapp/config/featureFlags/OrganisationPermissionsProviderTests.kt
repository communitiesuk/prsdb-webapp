package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_ORG_EDITOR
import uk.gov.communities.prsdb.webapp.services.OrganisationPermissionsProvider

class OrganisationPermissionsProviderTests : FeatureFlagTest() {
    @Autowired
    lateinit var organisationPermissionsProvider: OrganisationPermissionsProvider

    // TODO PDJB-1828: Remove this setup when the MULTI_USER_ORGANISATIONS feature flag is removed
    @BeforeEach
    fun enableMultiUserOrganisations() {
        featureFlagManager.enableFeature(MULTI_USER_ORGANISATIONS)
    }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    private fun setAuthenticatedRole(role: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("user", "password", listOf(SimpleGrantedAuthority(role)))
    }

    @Test
    fun `isCurrentUserOrgAdmin returns true for an organisation admin`() {
        setAuthenticatedRole(ROLE_ORG_ADMIN)

        assertTrue(organisationPermissionsProvider.isCurrentUserOrgAdmin())
    }

    @ParameterizedTest(name = "for a user with the {0} role")
    @ValueSource(strings = [ROLE_ORG_EDITOR, ROLE_INDIVIDUAL_LANDLORD, ROLE_LOCAL_COUNCIL_USER])
    fun `isCurrentUserOrgAdmin returns false for a user who is not an organisation admin`(role: String) {
        setAuthenticatedRole(role)

        assertFalse(organisationPermissionsProvider.isCurrentUserOrgAdmin())
    }

    @Test
    fun `isCurrentUserOrgAdmin returns false when there is no authenticated user`() {
        assertFalse(organisationPermissionsProvider.isCurrentUserOrgAdmin())
    }

    @ParameterizedTest(name = "for a user with the {0} role")
    @ValueSource(strings = [ROLE_ORG_ADMIN, ROLE_ORG_EDITOR, ROLE_INDIVIDUAL_LANDLORD, ROLE_LOCAL_COUNCIL_USER])
    fun `canCurrentUserPerformOrgAdminActions returns the result of isCurrentUserOrgAdmin`(role: String) {
        setAuthenticatedRole(role)

        assertEquals(
            organisationPermissionsProvider.isCurrentUserOrgAdmin(),
            organisationPermissionsProvider.canCurrentUserPerformOrgAdminActions(),
        )
    }

    @Test
    fun `canCurrentUserPerformOrgAdminActions returns the result of isCurrentUserOrgAdmin when there is no authenticated user`() {
        assertEquals(
            organisationPermissionsProvider.isCurrentUserOrgAdmin(),
            organisationPermissionsProvider.canCurrentUserPerformOrgAdminActions(),
        )
    }

    // TODO PDJB-1828: Remove this nested class when the MULTI_USER_ORGANISATIONS feature flag is removed
    @Nested
    inner class BeforePdjb1210MultiUserOrganisations {
        @BeforeEach
        fun disableMultiUserOrganisationsForBeforePdjb1210Tests() {
            featureFlagManager.disableFeature(MULTI_USER_ORGANISATIONS)
        }

        @ParameterizedTest(name = "for a user with the {0} role")
        @ValueSource(strings = [ROLE_ORG_ADMIN, ROLE_ORG_EDITOR])
        fun `canCurrentUserPerformOrgAdminActions returns true for any organisation user`(role: String) {
            setAuthenticatedRole(role)

            assertTrue(organisationPermissionsProvider.canCurrentUserPerformOrgAdminActions())
        }
    }
}
