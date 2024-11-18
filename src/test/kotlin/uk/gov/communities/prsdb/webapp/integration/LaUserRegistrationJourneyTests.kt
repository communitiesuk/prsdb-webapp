package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.PageNotFoundPage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.assertIsPage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService

class LaUserRegistrationJourneyTests : IntegrationTest() {
    @MockBean
    lateinit var invitationService: LocalAuthorityInvitationService

    @BeforeEach
    fun setup() {
        val testToken = "test token"
        whenever(invitationService.getTokenFromSession()).thenReturn(testToken)
        whenever(invitationService.tokenIsValid(testToken)).thenReturn(true)
    }

    @Nested
    inner class LaUserRegistrationLandingPage {
        @Test
        fun `Click submit redirects to the name step`() {
            val formPage = navigator.goToLaUserRegistrationLandingPage()
            assertThat(formPage.headingCaption).containsText("Before you register")
            assertThat(formPage.heading).containsText("Registering as a local authority user")

            val nextPage = formPage.submit()
            val namePage = assertIsPage(nextPage, NameFormPageLaUserRegistration::class)
            assertThat(namePage.fieldSetHeading).containsText("What is your full name?")
        }
    }

    @Nested
    inner class LaUserRegistrationStepName {
        @Test
        fun `Navigating directly to this step redirects to the first step`() {
            val firstStep = navigator.skipToLaUserRegistrationNameFormPage()
            assertThat(firstStep.heading).containsText("Registering as a local authority user")
        }

        @Test
        fun `Submitting a valid name redirects to the next step`() {
            val formPage = navigator.goToLaUserRegistrationNameFormPage()
            formPage.fillInput("Test User")
            val nextPage = formPage.submit()
            val emailPage = assertIsPage(nextPage, EmailFormPageLaUserRegistration::class)
            assertThat(emailPage.fieldSetHeading).containsText("What is your work email address?")
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val formPage = navigator.goToLaUserRegistrationNameFormPage()
            formPage.fillInput("")
            formPage.submitUnsuccessfully()
            assertThat(
                formPage.inputFormErrorMessage,
            ).containsText("You must enter your full name")
        }
    }

    @Nested
    inner class LaUserRegistrationStepEmail {
        @Test
        fun `Navigating directly to this step redirects to the first step`() {
            val firstStep = navigator.skipToLaUserRegistrationEmailFormPage()
            assertThat(firstStep.heading).containsText("Registering as a local authority user")
        }

        @Test
        fun `Submitting a valid email redirects to the next step`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillInput("test@example.com")
            // This will need to change when the "check answers" page is implemented
            val nextPage = formPage.submit()
            val notFoundPage = assertIsPage(nextPage, PageNotFoundPage::class)
            assertThat(notFoundPage.heading).containsText("Page not found")
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillInput("")
            formPage.submitUnsuccessfully()
            assertThat(
                formPage.inputFormErrorMessage,
            ).containsText(
                "Enter a valid email address to continue. An email is required for contact purposes.",
            )
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            formPage.fillInput("notAnEmail")
            formPage.submitUnsuccessfully()
            assertThat(
                formPage.inputFormErrorMessage,
            ).containsText("Enter an email address in the right format")
        }
    }
}
