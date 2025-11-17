package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.EmailFormPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.InvalidLinkPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.NameFormPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService

class LocalCouncilUserRegistrationSinglePageTests : IntegrationTestWithImmutableData("data-mockuser-not-local-council-user.sql") {
    @Autowired
    lateinit var localCouncilService: LocalCouncilService

    @Autowired
    lateinit var invitationService: LocalCouncilInvitationService

    lateinit var invitation: LocalCouncilInvitation

    @BeforeEach
    fun setup() {
        val token =
            invitationService.createInvitationToken(
                email = "anyEmail@test.com",
                council = localCouncilService.retrieveLocalCouncilById(2),
            )

        invitation = invitationService.getInvitationFromToken(token)
    }

    @Nested
    inner class LaUserRegistrationAcceptInvitationRoute {
        @Test
        fun `Navigating here with an invalid token redirects to the invalid link page`(page: Page) {
            val invalidToken = "1234abcd-5678-abcd-1234-567abcd1111d"
            navigator.navigateToLaUserRegistrationAcceptInvitationRoute(invalidToken)
            val invalidLinkPage = BasePage.assertPageIs(page, InvalidLinkPageLocalCouncilUserRegistration::class)
            BaseComponent.assertThat(invalidLinkPage.heading).containsText("This invite link is not valid")
            assertThat(
                invalidLinkPage.description,
            ).containsText("Contact the PRS Database admin user at your local council to ask for another invite.")
        }
    }

    @Nested
    inner class LaUserRegistrationStepLandingPage : NestedIntegrationTestWithImmutableData("data-local.sql") {
        @Test
        fun `Navigating here as a registered local council user redirects to the Local Council dashboard page`(page: Page) {
            navigator.navigateToLaUserRegistrationLandingPage(invitation.token)
            val dashboardPage = BasePage.assertPageIs(page, LocalCouncilDashboardPage::class)
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
    inner class LaUserRegistrationPrivacyNoticeName {
        @Test
        fun `Submitting without confirming returns an error`() {
            val privacyNoticePage = navigator.skipToLaUserRegistrationPrivacyNoticePage(invitation.token)
            privacyNoticePage.form.submit()
            assertThat(privacyNoticePage.form.getErrorMessage()).containsText("You must confirm you’ve read the privacy notice to continue")
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
            checkAnswersPage.summaryList.nameRow
                .clickActionLinkAndWait()
            BasePage.assertPageIs(page, NameFormPageLocalCouncilUserRegistration::class)
        }

        @Test
        fun `Change Email link navigates to the email step`(page: Page) {
            val checkAnswersPage = navigator.skipToLaUserRegistrationCheckAnswersPage(invitation.token)
            checkAnswersPage.summaryList.emailRow
                .clickActionLinkAndWait()
            BasePage.assertPageIs(page, EmailFormPageLocalCouncilUserRegistration::class)
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
