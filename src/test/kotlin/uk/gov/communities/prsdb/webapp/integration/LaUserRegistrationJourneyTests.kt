package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.assertIsPage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages.SuccessPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages.SummaryPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService

@Sql("/data-local.sql")
class LaUserRegistrationJourneyTests : IntegrationTest() {
    @MockBean
    lateinit var invitationService: LocalAuthorityInvitationService

    @BeforeEach
    fun setup() {
        val testToken = "test token"
        whenever(invitationService.getTokenFromSession()).thenReturn(testToken)
        whenever(invitationService.tokenIsValid(testToken)).thenReturn(true)
        val testLocalAuthority = LocalAuthority(1, "Test Authority")
        whenever(invitationService.getAuthorityForToken(testToken)).thenReturn(testLocalAuthority)
    }

    @Nested
    inner class LaUserRegistrationLandingPage {
        @Test
        fun `Page renders when we navigate to it`() {
            val formPage = navigator.goToLaUserRegistrationLandingPage()
            assertThat(formPage.headingCaption).containsText("Before you register")
            assertThat(formPage.heading).containsText("Registering as a local authority user")
        }

        @Test
        fun `Click submit redirects to the name step`() {
            val formPage = navigator.goToLaUserRegistrationLandingPage()
            val nextPage = formPage.submit()
            assertIsPage(nextPage, NameFormPageLaUserRegistration::class)
        }
    }

    @Nested
    inner class LaUserRegistrationStepName {
        @Test
        fun `Page renders when we navigate to this step through the registration journey`() {
            val formPage = navigator.goToLaUserRegistrationNameFormPage()
            assertThat(formPage.fieldSetHeading).containsText("What is your full name?")
        }

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
            assertIsPage(nextPage, EmailFormPageLaUserRegistration::class)
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
        fun `Page renders when we navigate to this step through the registration journey`() {
            val formPage = navigator.goToLaUserRegistrationEmailFormPage()
            assertThat(formPage.fieldSetHeading).containsText("What is your work email address?")
        }

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
            assertIsPage(nextPage, SummaryPageLaUserRegistration::class)
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

    @Nested
    inner class LaUserRegistrationCheckAnswers {
        @Test
        fun `Page renders when we navigate to this step through the registration journey`() {
            val formPage = navigator.goToLaUserRegistrationCheckAnswersPage()
            assertThat(formPage.heading).containsText("Check your answers")
        }

        @Test
        fun `Navigating directly to this step redirects to the first step`() {
            val renderedPage = navigator.skipToLaUserRegistrationCheckAnswersFormPage()
            assertThat(renderedPage.heading).containsText("Registering as a local authority user")
        }

        @Test
        fun `Change Name link navigates to the name step`() {
            val formPage = navigator.goToLaUserRegistrationCheckAnswersPage()
            val linkedPage = formPage.changeName()
            assertIsPage(linkedPage, NameFormPageLaUserRegistration::class)
        }

        @Test
        fun `Change Email link navigates to the email step`() {
            val formPage = navigator.goToLaUserRegistrationCheckAnswersPage()
            val linkedPage = formPage.changeEmail()
            assertIsPage(linkedPage, EmailFormPageLaUserRegistration::class)
        }

        @Test
        fun `Submitting redirects to the success page`() {
            val formPage = navigator.goToLaUserRegistrationCheckAnswersPage()
            assertThat(formPage.heading).containsText("Check your answers")
            val nextPage = formPage.submit()
            assertIsPage(nextPage, SuccessPageLaUserRegistration::class)
        }
    }

    @Nested
    inner class LaUserRegistrationSuccess {
        // TODO: PRSD-541 - this is currently failing because we are logged in as Mock User, who is already an LA user.
        // localAuthorityDataService.registerNewUser(...) fails because it can't add a second user to the table with the same One Login id

       /* @Test
        fun `Page renders when we navigate to this step through the registration journey`() {
            val successPage = navigator.goToLaUserRegistrationSuccessPage()
            assertThat(successPage.bannerHeading).containsText("You've registered as a Test Authority user")
            assertThat(successPage.bodyHeading).containsText("What happens next?")
        }

        // TODO: PRSD-541 - we need the page to render successfully when it should before checking this to make sure it is failing for the correct reason
        @Test
        fun `Navigating directly to here with an incomplete form returns a 500 error page`() {
        }*/
    }
}
