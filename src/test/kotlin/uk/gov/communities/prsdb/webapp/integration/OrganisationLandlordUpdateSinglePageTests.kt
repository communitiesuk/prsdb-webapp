package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType

@WithOrgLandlordProfile
class OrganisationLandlordUpdateSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun setup() {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Nested
    inner class AddingTrustInterruption {
        @Test
        fun `shows the adding trust page when adding trust to organisation type`(page: Page) {
            val interruptionPage = navigator.skipToUpdateOrgTypeTrustInterruptionPage(listOf(OrgType.TRUST))

            assertThat(interruptionPage.heading).containsText("You must provide trustee details")
        }
    }

    @Nested
    inner class RemovingTrustInterruption : NestedIntegrationTestWithImmutableData("data-org-landlord-trust.sql") {
        @BeforeEach
        fun setup() {
            featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
        }

        @Test
        fun `shows the removing trust page when removing trust from organisation type`(page: Page) {
            val interruptionPage = navigator.skipToUpdateOrgTypeTrustInterruptionPage(listOf(OrgType.COMPANY))

            assertThat(interruptionPage.heading).containsText("Are you sure you want to change this?")
            assertThat(interruptionPage.body).containsText("from a trust to company")
        }

        @Test
        fun `shows comma-separated org types when multiple are selected`(page: Page) {
            val interruptionPage = navigator.skipToUpdateOrgTypeTrustInterruptionPage(listOf(OrgType.COMPANY, OrgType.CHARITY))

            assertThat(interruptionPage.body).containsText("from a trust to company, charity")
        }
    }
}
