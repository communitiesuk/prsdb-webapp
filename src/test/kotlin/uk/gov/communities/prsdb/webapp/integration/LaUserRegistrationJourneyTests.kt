package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.laUserRegistrationJourneyPages.SuccessPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.laUserRegistrationJourneyPages.SummaryPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService

@Sql("/data-mockuser-not-lauser.sql")
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
            val landingPage = navigator.goToLaUserRegistrationLandingPage()
            assertThat(landingPage.headingCaption).containsText("Before you register")
            assertThat(landingPage.heading).containsText("Registering as a local authority user")
        }

        @Test
        fun `Clicking submit redirects to the name step`(page: Page) {
            val landingPage = navigator.goToLaUserRegistrationLandingPage()
            landingPage.clickBeginButton()
            assertPageIs(page, NameFormPageLaUserRegistration::class)
        }
    }

    @Nested
    inner class LaUserRegistrationStepName {
        @Test
        fun `Page renders when we navigate to this step through the registration journey`() {
            val namePage = navigator.goToLaUserRegistrationNameFormPage()
            assertThat(namePage.form.getFieldsetHeading()).containsText("What is your full name?")
        }

        @Test
        fun `Submitting a valid name redirects to the next step`(page: Page) {
            val namePage = navigator.goToLaUserRegistrationNameFormPage()
            namePage.nameInput.fill("Test User")
            namePage.form.submit()
            assertPageIs(page, EmailFormPageLaUserRegistration::class)
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val namePage = navigator.goToLaUserRegistrationNameFormPage()
            namePage.nameInput.fill("")
            namePage.form.submit()
            assertThat(namePage.form.getErrorMessage()).containsText("You must enter your full name")
        }
    }

    @Nested
    inner class LaUserRegistrationStepEmail {
        @Test
        fun `Page renders when we navigate to this step through the registration journey`() {
            val emailPage = navigator.goToLaUserRegistrationEmailFormPage()
            assertThat(emailPage.form.getFieldsetHeading()).containsText("What is your work email address?")
        }

        @Test
        fun `Submitting a valid email redirects to the next step`(page: Page) {
            val emailPage = navigator.goToLaUserRegistrationEmailFormPage()
            emailPage.emailInput.fill("test@example.com")
            emailPage.form.submit()
            assertPageIs(page, SummaryPageLaUserRegistration::class)
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val emailPage = navigator.goToLaUserRegistrationEmailFormPage()
            emailPage.emailInput.fill("")
            emailPage.form.submit()
            assertThat(
                emailPage.form.getErrorMessage(),
            ).containsText(
                "Enter a valid email address to continue. An email is required for contact purposes.",
            )
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val emailPage = navigator.goToLaUserRegistrationEmailFormPage()
            emailPage.emailInput.fill("notAnEmail")
            emailPage.form.submit()
            assertThat(
                emailPage.form.getErrorMessage(),
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
        fun `Change Name link navigates to the name step`() {
            val formPage = navigator.goToLaUserRegistrationCheckAnswersPage()
            val linkedPage = formPage.changeName()
            assertPageIs(linkedPage, NameFormPageLaUserRegistration::class)
        }

        @Test
        fun `Change Email link navigates to the email step`() {
            val formPage = navigator.goToLaUserRegistrationCheckAnswersPage()
            val linkedPage = formPage.changeEmail()
            assertPageIs(linkedPage, EmailFormPageLaUserRegistration::class)
        }

        @Test
        fun `Submitting redirects to the success page`() {
            val formPage = navigator.goToLaUserRegistrationCheckAnswersPage()
            assertThat(formPage.heading).containsText("Check your answers")
            val nextPage = formPage.submit()
            assertPageIs(nextPage, SuccessPageLaUserRegistration::class)
        }
    }

    @Nested
    inner class LaUserRegistrationSuccess {
        @Test
        fun `Page renders when we navigate to this step through the registration journey`() {
            val successPage = navigator.goToLaUserRegistrationSuccessPage()
            assertThat(successPage.bannerHeading).containsText("You've registered as a Test Authority user")
            assertThat(successPage.bodyHeading).containsText("What happens next")
        }

        @Test
        fun `Navigating directly to here with an incomplete form returns a 500 error page`() {
            val successPage = navigator.skipToLaUserRegistrationSuccessPage()
            assertThat(successPage.errorHeading).containsText("Sorry, there is a problem with the service")
        }
    }
}
