package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnsPropertyFormPagePropertyRegistration

class PropertyRegistrationOwnershipTypeSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class OwnsPropertyStep {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val ownsPropertyPage = navigator.skipToPropertyRegistrationOwnsPropertyPage()
            ownsPropertyPage.form.submit()
            assertThat(ownsPropertyPage.form.getErrorMessage())
                .containsText("Select if you own this property")
        }
    }

    @Nested
    inner class OwnershipKindStep {
        @Test
        fun `The freehold and leasehold checkboxes show their hint text`() {
            val ownershipKindPage = navigator.skipToPropertyRegistrationOwnershipKindPage()

            assertThat(ownershipKindPage.form.checkboxes).containsText("Own the property and land indefinitely")
            assertThat(ownershipKindPage.form.checkboxes).containsText("Have a lease with a freeholder (includes most flats)")
        }

        @Test
        fun `Submitting with no option selected returns an error`() {
            val ownershipKindPage = navigator.skipToPropertyRegistrationOwnershipKindPage()

            ownershipKindPage.form.submit()

            assertThat(ownershipKindPage.form.getErrorMessage()).containsText("Select how you own this property")
        }

        @Test
        fun `Back link returns to the owns property page`(page: Page) {
            val ownershipKindPage = navigator.skipToPropertyRegistrationOwnershipKindPage()

            ownershipKindPage.backLink.clickAndWait()

            assertPageIs(page, OwnsPropertyFormPagePropertyRegistration::class)
        }
    }
}
