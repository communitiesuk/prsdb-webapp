package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.CheckAnswersPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.ConfirmationPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import java.util.UUID

@Sql("/data-mockuser-not-lauser.sql")
class LaUserRegistrationJourneyTests : IntegrationTest() {
    @SpyBean
    lateinit var localAuthorityService: LocalAuthorityService

    @MockBean
    lateinit var invitationService: LocalAuthorityInvitationService

    lateinit var invitation: LocalAuthorityInvitation

    @BeforeEach
    fun setup() {
        invitation =
            LocalAuthorityInvitation(
                id = 0L,
                token = UUID.randomUUID(),
                email = "anyEmail@test.com",
                invitingAuthority = localAuthorityService.retrieveLocalAuthorityById(1),
            )

        whenever(invitationService.getTokenFromSession()).thenReturn(invitation.token.toString())
        whenever(invitationService.getInvitationFromToken(invitation.token.toString())).thenReturn(invitation)
        whenever(invitationService.tokenIsValid(invitation.token.toString())).thenCallRealMethod()
        whenever(invitationService.getAuthorityForToken(invitation.token.toString())).thenCallRealMethod()
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in`(page: Page) {
        // Landing page - render
        val landingPage = navigator.goToLaUserRegistrationLandingPage()
        assertThat(landingPage.headingCaption).containsText("Before you register")
        assertThat(landingPage.heading).containsText("Registering as a local authority user")
        // Submit and go to next page
        landingPage.clickBeginButton()
        val namePage = assertPageIs(page, NameFormPageLaUserRegistration::class)

        // Name page - render
        assertThat(namePage.form.getFieldsetHeading()).containsText("What is your full name?")
        // Fill in, submit and go to next page
        namePage.nameInput.fill("Test User")
        namePage.form.submit()
        val emailPage = assertPageIs(page, EmailFormPageLaUserRegistration::class)

        // Email page - render
        assertThat(emailPage.form.getFieldsetHeading()).containsText("What is your work email address?")
        // Fill in, submit and go to next page
        emailPage.emailInput.fill("test@example.com")
        emailPage.form.submit()
        val checkAnswersPage = assertPageIs(page, CheckAnswersPageLaUserRegistration::class)

        // Check answers page - render
        assertThat(checkAnswersPage.heading).containsText("Check your answers")
        // Submit and go to next page
        checkAnswersPage.form.submit()
        val confirmationPage = assertPageIs(page, ConfirmationPageLaUserRegistration::class)

        verify(invitationService).deleteInvitation(invitation)

        // Confirmation page - render
        assertThat(confirmationPage.bannerHeading).containsText("You've registered as a ${invitation.invitingAuthority.name} user")
        assertThat(confirmationPage.bodyHeading).containsText("What happens next")
    }

    @Nested
    inner class LaUserRegistrationStepName {
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
        fun `Change Name link navigates to the name step`(page: Page) {
            val checkAnswersPage = navigator.goToLaUserRegistrationCheckAnswersPage()
            checkAnswersPage.form.summaryList.nameRow
                .clickActionLinkAndWait()
            assertPageIs(page, NameFormPageLaUserRegistration::class)
        }

        @Test
        fun `Change Email link navigates to the email step`(page: Page) {
            val checkAnswersPage = navigator.goToLaUserRegistrationCheckAnswersPage()
            checkAnswersPage.form.summaryList.emailRow
                .clickActionLinkAndWait()
            assertPageIs(page, EmailFormPageLaUserRegistration::class)
        }
    }

    @Nested
    inner class LaUserRegistrationSuccess {
        @Test
        fun `Navigating directly to here with an incomplete form returns a 400 error page`() {
            val errorPage = navigator.skipToLaUserRegistrationConfirmationPage()
            assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }
    }
}
