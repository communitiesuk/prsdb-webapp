package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
}
