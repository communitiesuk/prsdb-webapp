package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasAnyFreeholdersFormPagePropertyRegistration

class PropertyRegistrationFreeholdersSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class FreeholdersListStep {
        @Test
        fun `Joint freeholders see the joint freeholders heading`() {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage(isJointFreeholder = true)
            assertThat(freeholdersListPage.heading).containsText("You’ve added 3 joint freeholders")
        }

        @Test
        fun `Leasehold only landlords see the single freeholders heading`() {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage(isJointFreeholder = false)
            assertThat(freeholdersListPage.heading).containsText("You’ve added 3 freeholders")
        }

        // TODO PDJB-1969: update the expected page when has any freeholders becomes an internal step
        @Test
        fun `Back link returns to the has any freeholders page`(page: Page) {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage()
            freeholdersListPage.backLink.clickAndWait()
            assertPageIs(page, HasAnyFreeholdersFormPagePropertyRegistration::class)
        }
    }
}
