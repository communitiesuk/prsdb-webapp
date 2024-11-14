package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.PageNotFoundPage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration

class LaUserRegistrationJourneyTests : IntegrationTest() {
    @Nested
    inner class LaUserRegistrationStepName {
        @Test
        fun `Submitting a valid name redirects to the next step`() {
            val formPage = navigator.goToLaUserRegistrationNameFormPage()
            formPage.fillInput("Test User")
            val nextStep = formPage.submit()
            BasePage.createAndValidate(nextStep, EmailFormPageLaUserRegistration::class)
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val formPage = navigator.goToLaUserRegistrationNameFormPage()
            formPage.fillInput("")
            formPage.submitUnsuccessfully()
            formPage.inputFormGroup.assertErrorMessageContains("You must enter your full name")
        }
    }

    @Nested
    inner class LaUserRegistrationStepEmail {
        @Test
        fun `Navigating directly to this step redirects to the name step`() {
            val firstStep = navigator.skipToLaUserRegistrationEmailFormPage()
            assertThat(firstStep.fieldSetHeading).containsText("What is your full name?")
        }

        @Test
        fun `Submitting a valid email redirects to the next step`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillInput("test@example.com")
            val nextStep = formPage.submit()
            // This will need to change when the "check answers" page is implemented
            BasePage.createAndValidate(nextStep, PageNotFoundPage::class)
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillInput("")
            formPage.submitUnsuccessfully()
            formPage.inputFormGroup.assertErrorMessageContains(
                "Enter a valid email address to continue. An email is required for contact purposes.",
            )
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillInput("notAnEmail")
            formPage.submitUnsuccessfully()
            formPage.inputFormGroup.assertErrorMessageContains("Enter an email address in the right format")
        }
    }
}
