package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT

class DelegateToLettingAgentSinglePageTests : IntegrationTestWithMutableData("data-local.sql") {
    companion object {
        const val PROPERTY_OWNERSHIP_ID = 4L
    }

    @BeforeEach
    fun enableFlag() {
        featureFlagManager.enableFeature(DELEGATE_TO_LETTING_AGENT)
    }

    @Nested
    inner class AllowLettingAgentPage {
        @Test
        fun `submitting an empty email address returns an error`() {
            val allowLettingAgentPage = navigator.goToDelegateToLettingAgentAllowLettingAgentPage(PROPERTY_OWNERSHIP_ID)

            allowLettingAgentPage.submitEmail("")

            assertThat(allowLettingAgentPage.form.getErrorMessage()).containsText("Enter an email address")
        }

        @Test
        fun `submitting an invalid email address returns an error`() {
            val allowLettingAgentPage = navigator.goToDelegateToLettingAgentAllowLettingAgentPage(PROPERTY_OWNERSHIP_ID)

            allowLettingAgentPage.submitEmail("notAnEmail")

            assertThat(allowLettingAgentPage.form.getErrorMessage()).containsText("Enter an email address in the right format")
        }

        @Test
        fun `submitting the landlord's own email address returns an error`() {
            val allowLettingAgentPage = navigator.goToDelegateToLettingAgentAllowLettingAgentPage(PROPERTY_OWNERSHIP_ID)

            allowLettingAgentPage.submitEmail("alex.surname@example.com")

            assertThat(allowLettingAgentPage.form.getErrorMessage())
                .containsText("You cannot enter yourself as the letting agent or property manager")
        }
    }
}
