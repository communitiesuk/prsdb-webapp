package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.EmailFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages.NameFormPageLaUserRegistration
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService

class LaUserRegistrationSinglePageTests : SinglePageTestWithSeedData("data-mockuser-not-lauser.sql") {
    @Autowired
    lateinit var localAuthorityService: LocalAuthorityService

    @Autowired
    lateinit var invitationService: LocalAuthorityInvitationService

    lateinit var invitation: LocalAuthorityInvitation

    @BeforeEach
    fun setup() {
        val token =
            invitationService.createInvitationToken(
                email = "anyEmail@test.com",
                authority = localAuthorityService.retrieveLocalAuthorityById(2),
            )

        invitation = invitationService.getInvitationFromToken(token)
    }

    @Nested
    inner class LaUserRegistrationAcceptInvitationRoute : NestedSinglePageTestWithSeedData("data-mockuser-with-expired-invitation.sql") {
        @Test
        fun `Navigating here with an expired token redirects to the invalid link page`(page: Page) {
            val expiredToken = "1234abcd-5678-abcd-1234-567abcd1111a"
            navigator.navigateToLaUserRegistrationAcceptInvitationRoute(expiredToken)
            val errorPage = BasePage.assertPageIs(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("This invite link is not valid")
            assertThat(
                errorPage.description,
            ).containsText("Contact the PRS Database admin user at your local council to ask for another invite.")
        }

        @Test
        fun `Navigating here with an invalid token redirects to the invalid link page`(page: Page) {
            val invalidToken = "1234abcd-5678-abcd-1234-567abcd1111d"
            navigator.navigateToLaUserRegistrationAcceptInvitationRoute(invalidToken)
            val errorPage = BasePage.assertPageIs(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("This invite link is not valid")
            assertThat(
                errorPage.description,
            ).containsText("Contact the PRS Database admin user at your local council to ask for another invite.")
        }
    }

    @Nested
    inner class LaUserRegistrationStepLandingPage : NestedSinglePageTestWithSeedData("data-local.sql") {
        @Test
        fun `Navigating here as a registered local authority user redirects to the LA dashboard page`(page: Page) {
            navigator.navigateToLaUserRegistrationLandingPage(invitation.token)
            val dashboardPage = BasePage.assertPageIs(page, LocalAuthorityDashboardPage::class)
            BaseComponent.assertThat(dashboardPage.bannerHeading).containsText("Mock User")
            BaseComponent.assertThat(dashboardPage.bannerSubHeading).containsText("BATH AND NORTH EAST SOMERSET COUNCIL")
        }
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
            BasePage.assertPageIs(page, NameFormPageLaUserRegistration::class)
        }

        @Test
        fun `Change Email link navigates to the email step`(page: Page) {
            val checkAnswersPage = navigator.skipToLaUserRegistrationCheckAnswersPage(invitation.token)
            checkAnswersPage.form.summaryList.emailRow
                .clickActionLinkAndWait()
            BasePage.assertPageIs(page, EmailFormPageLaUserRegistration::class)
        }
    }

    @Nested
    inner class LaUserRegistrationSuccess {
        @Test
        fun `Navigating directly to here with an incomplete form returns a 400 error page`(page: Page) {
            navigator.navigateToLaUserRegistrationConfirmationPage()
            val errorPage = BasePage.assertPageIs(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }
    }
}
