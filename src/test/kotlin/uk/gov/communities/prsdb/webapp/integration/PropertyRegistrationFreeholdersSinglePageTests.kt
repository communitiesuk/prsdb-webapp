package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderProvideDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasAnyFreeholdersFormPagePropertyRegistration

class PropertyRegistrationFreeholdersSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class FreeholderNameStep {
        @Test
        fun `Joint freeholders see the joint freeholder heading`() {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage()
            assertThat(freeholderNamePage.heading).containsText("What is this joint freeholder’s name?")
        }

        @Test
        fun `Non-joint freeholders see the non-joint freeholder heading`() {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage(jointFreeholders = false)
            assertThat(freeholderNamePage.heading).containsText("What is this freeholder’s name?")
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage()
            freeholderNamePage.submitName("")
            assertThat(freeholderNamePage.form.getErrorMessage()).containsText("Enter a name")
        }

        @Test
        fun `Back link returns to the provide details page`(page: Page) {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage()
            freeholderNamePage.backLink.clickAndWait()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class FreeholdersListStep {
        @Test
        fun `Joint freeholders see the joint freeholders heading`() {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage()
            assertThat(freeholdersListPage.heading).containsText("You’ve added 3 joint freeholders")
        }

        @Test
        fun `Non-joint freeholders see the non-joint freeholders heading`() {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage(jointFreeholders = false)
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
