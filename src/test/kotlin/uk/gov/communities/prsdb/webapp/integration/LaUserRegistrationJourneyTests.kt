package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.CheckAnswersPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.ConfirmationPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService

@Sql("/data-mockuser-not-lauser.sql")
class LaUserRegistrationJourneyTests : IntegrationTest() {
    @Autowired
    lateinit var localAuthorityService: LocalAuthorityService

    @Autowired
    lateinit var invitationService: LocalAuthorityInvitationService

    @MockitoSpyBean
    lateinit var laUserRepository: LocalAuthorityUserRepository

    @MockitoSpyBean
    lateinit var invitationRepository: LocalAuthorityInvitationRepository

    lateinit var invitation: LocalAuthorityInvitation

    @BeforeEach
    fun setup() {
        val token =
            invitationService.createInvitationToken(
                email = "anyEmail@test.com",
                authority = localAuthorityService.retrieveLocalAuthorityById(1),
            )

        invitation = invitationService.getInvitationFromToken(token)
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in`(page: Page) {
        // Landing page - render
        val landingPage = navigator.skipToLaUserRegistrationLandingPage(invitation.token)
        assertThat(landingPage.headingCaption).containsText("Before you register")
        assertThat(landingPage.heading).containsText("Registering as a local authority user")
        // Submit and go to next page
        landingPage.clickBeginButton()
        val namePage = assertPageIs(page, NameFormPageLaUserRegistration::class)

        // Name page - render
        assertThat(namePage.form.fieldsetHeading).containsText("What is your full name?")
        // Fill in, submit and go to next page
        namePage.submitName("Test User")
        val emailPage = assertPageIs(page, EmailFormPageLaUserRegistration::class)

        // Email page - render
        assertThat(emailPage.form.fieldsetHeading).containsText("What is your work email address?")
        assertThat(emailPage.form.emailInput).hasValue(invitation.invitedEmail)
        // Fill in, submit and go to next page
        emailPage.submitEmail("test@example.com")
        val checkAnswersPage = assertPageIs(page, CheckAnswersPageLaUserRegistration::class)

        // Check answers page - render
        assertThat(checkAnswersPage.form.fieldsetHeading).containsText("Check your answers")
        // Submit and go to next page
        checkAnswersPage.form.submit()
        val confirmationPage = assertPageIs(page, ConfirmationPageLaUserRegistration::class)

        val invitationCaptor = captor<LocalAuthorityInvitation>()
        verify(invitationRepository).delete(invitationCaptor.capture())
        assertEquals(invitation.token, invitationCaptor.value.token)

        // Confirmation page - render
        val laUserCaptor = captor<LocalAuthorityUser>()
        verify(laUserRepository).save(laUserCaptor.capture())

        assertThat(confirmationPage.bannerHeading).containsText("You've registered as a ${laUserCaptor.value.localAuthority.name} user")
        assertThat(confirmationPage.bodyHeading).containsText("What happens next")

        // Return to dashboard button
        confirmationPage.returnToDashboardButton.clickAndWait()
        val dashboard = assertPageIs(page, LocalAuthorityDashboardPage::class)

        assertThat(dashboard.bannerSubHeading).containsText("Local authority")
    }

    @Nested
    inner class LaUserRegistrationStepName {
        @Test
        fun `Submitting an empty name returns an error`() {
            val namePage = navigator.skipToLaUserRegistrationNameFormPage(invitation.token)
            namePage.submitName("")
            assertThat(namePage.form.getErrorMessage()).containsText("You must enter your full name")
        }
    }

    @Nested
    inner class LaUserRegistrationStepEmail {
        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val emailPage = navigator.skipToLaUserRegistrationEmailFormPage(invitation.token)
            emailPage.submitEmail("")
            assertThat(emailPage.form.getErrorMessage())
                .containsText("Enter a valid email address to continue. An email is required for contact purposes.")
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val emailPage = navigator.skipToLaUserRegistrationEmailFormPage(invitation.token)
            emailPage.submitEmail("notAnEmail")
            assertThat(emailPage.form.getErrorMessage()).containsText("Enter an email address in the right format")
        }
    }

    @Nested
    inner class LaUserRegistrationCheckAnswers {
        @Test
        fun `Change Name link navigates to the name step`(page: Page) {
            val checkAnswersPage = navigator.skipToLaUserRegistrationCheckAnswersPage(invitation.token)
            checkAnswersPage.form.summaryList.nameRow
                .clickActionLinkAndWait()
            assertPageIs(page, NameFormPageLaUserRegistration::class)
        }

        @Test
        fun `Change Email link navigates to the email step`(page: Page) {
            val checkAnswersPage = navigator.skipToLaUserRegistrationCheckAnswersPage(invitation.token)
            checkAnswersPage.form.summaryList.emailRow
                .clickActionLinkAndWait()
            assertPageIs(page, EmailFormPageLaUserRegistration::class)
        }
    }

    @Nested
    inner class LaUserRegistrationSuccess {
        @Test
        fun `Navigating directly to here with an incomplete form returns a 400 error page`(page: Page) {
            navigator.navigateToLaUserRegistrationConfirmationPage()
            val errorPage = assertPageIs(page, ErrorPage::class)
            assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }
    }
}
