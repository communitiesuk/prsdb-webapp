package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat

class PropertyRegistrationOwnershipTypeSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class OwnershipKindStep {
        @Test
        fun `The page shows the heading, collapsed share of freehold details and checkbox hints`() {
            val ownershipKindPage = navigator.skipToPropertyRegistrationOwnershipKindPage()

            assertThat(ownershipKindPage.form.fieldsetHeading).containsText("What kind of ownership do you have?")
            assertThat(ownershipKindPage.shareOfFreeholdDetails).containsText("If you have a ‘share of freehold’")
            assertThat(ownershipKindPage.shareOfFreeholdDetails).not().hasAttribute("open", "")
            assertThat(ownershipKindPage.form.checkboxes).containsText("Own the property and land indefinitely")
            assertThat(ownershipKindPage.form.checkboxes).containsText("Have a lease with a freeholder (includes most flats)")
        }
    }
}
