package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderNameFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderProvideDetailsFormPagePropertyRegistration

class PropertyRegistrationFreeholdersSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class FreeholderNameStep {
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

        @Test
        fun `Submitted name is shown when returning to the page`(page: Page) {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage()
            freeholderNamePage.submitName("Jane Smith")
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class)
            BackLink.default(page).clickAndWait()
            val returnedNamePage = assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class)
            assertThat(returnedNamePage.form.nameInput.locator).hasValue("Jane Smith")
        }
    }
}
