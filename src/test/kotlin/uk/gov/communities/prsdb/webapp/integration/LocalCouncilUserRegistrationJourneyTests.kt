package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilUser
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilUserRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.CheckAnswersPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.ConfirmationPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.EmailFormPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.InvalidLinkPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.LandingPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.NameFormPageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages.PrivacyNoticePageLocalCouncilUserRegistration
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import java.net.URI

class LocalCouncilUserRegistrationJourneyTests : IntegrationTestWithMutableData("data-mockuser-not-lauser.sql") {
    @Autowired
    lateinit var localCouncilService: LocalCouncilService

    @Autowired
    lateinit var invitationService: LocalCouncilInvitationService

    @MockitoSpyBean
    lateinit var laUserRepository: LocalCouncilUserRepository

    @MockitoSpyBean
    lateinit var invitationRepository: LocalCouncilInvitationRepository

    @MockitoSpyBean
    override lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    lateinit var invitation: LocalCouncilInvitation

    @BeforeEach
    fun setup() {
        val token =
            invitationService.createInvitationToken(
                email = "anyEmail@test.com",
                council = localCouncilService.retrieveLocalCouncilById(2),
            )

        invitation = invitationService.getInvitationFromToken(token)

        whenever(absoluteUrlProvider.buildLocalCouncilDashboardUri()).thenReturn(URI.create("http://localhost/dashboard"))
    }

    @Test
    fun `User can navigate the whole journey if pages are correctly filled in`(page: Page) {
        // Accept invitation route
        navigator.navigateToLaUserRegistrationAcceptInvitationRoute(invitation.token.toString())
        val landingPage = assertPageIs(page, LandingPageLocalCouncilUserRegistration::class)
        // Landing page - render
        assertThat(landingPage.headingCaption).containsText("Before you register")
        assertThat(landingPage.heading).containsText("Registering as a local council user")
        // Submit and go to next page
        landingPage.clickBeginButton()
        val privacyNoticePage = assertPageIs(page, PrivacyNoticePageLocalCouncilUserRegistration::class)

        // Privacy notice page
        privacyNoticePage.form.iAgreeCheckbox.check()
        privacyNoticePage.form.submit()
        val namePage = assertPageIs(page, NameFormPageLocalCouncilUserRegistration::class)

        // Name page - render
        assertThat(namePage.form.fieldsetHeading).containsText("What is your full name?")
        // Fill in, submit and go to next page
        namePage.submitName("Test User")
        val emailPage = assertPageIs(page, EmailFormPageLocalCouncilUserRegistration::class)

        // Email page - render
        assertThat(emailPage.form.fieldsetHeading).containsText("What is your work email address?")
        assertThat(emailPage.form.emailInput).hasValue(invitation.invitedEmail)
        // Fill in, submit and go to next page
        emailPage.submitEmail("test@example.com")
        val checkAnswersPage = assertPageIs(page, CheckAnswersPageLocalCouncilUserRegistration::class)

        // Check answers page - render
        assertThat(checkAnswersPage.heading).containsText("Check your answers")
        // Submit and go to next page
        checkAnswersPage.form.submit()
        val confirmationPage = assertPageIs(page, ConfirmationPageLocalCouncilUserRegistration::class)

        val invitationCaptor = captor<LocalCouncilInvitation>()
        verify(invitationRepository).delete(invitationCaptor.capture())
        assertEquals(invitation.token, invitationCaptor.value.token)

        // Confirmation page - render
        val laUserCaptor = captor<LocalCouncilUser>()
        verify(laUserRepository).save(laUserCaptor.capture())

        assertThat(confirmationPage.bannerHeading).containsText("You’ve registered as a ${laUserCaptor.value.localCouncil.name} user")
        assertThat(confirmationPage.bodyHeading).containsText("What happens next")

        // Return to dashboard button
        confirmationPage.returnToDashboardButton.clickAndWait()
        val dashboard = assertPageIs(page, LocalCouncilDashboardPage::class)

        assertThat(dashboard.bannerSubHeading).containsText("Local council")
    }

    @Nested
    inner class WithExpiredToken : NestedIntegrationTestWithMutableData("data-mockuser-with-expired-invitation.sql") {
        @Test
        fun `User with an expired token is redirected to the invalid link page`(page: Page) {
            val expiredToken = "1234abcd-5678-abcd-1234-567abcd1111a"
            navigator.navigateToLaUserRegistrationAcceptInvitationRoute(expiredToken)
            val invalidLinkPage = assertPageIs(page, InvalidLinkPageLocalCouncilUserRegistration::class)
            assertThat(invalidLinkPage.heading).containsText("This invite link is not valid")
            assertThat(
                invalidLinkPage.description,
            ).containsText("Contact the PRS Database admin user at your local council to ask for another invite.")
        }
    }
}
