package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithMutableData.NestedIntegrationTestWithMutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.CheckAnswersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.EmailAddressPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.FullNamePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.InvalidLinkPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.JoinOrganisationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.UserIsAlreadyLandlordPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AcceptOrganisationInvitationJourneyTests :
    IntegrationTestWithMutableData(listOf("data-local.sql", "data-org-invitation-mockuser-not-landlord.sql")) {
    private val validToken = "1234abcd-5678-abcd-1234-567abcd2222a"

    private val expiredToken = "1234abcd-5678-abcd-1234-567abcd2222d"

    private val unrecognisedToken = "1234abcd-5678-abcd-1234-567abcd9999a"

    @Autowired
    private lateinit var invitationRepository: OrganisationalLandlordInvitationRepository

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(MULTI_USER_ORGANISATIONS)
    }

    @Test
    fun `Invitees can successfully accept an invitation to join an organisation`(page: Page) {
        // 1. Go to the start of accept invitation journey - a valid token for a non-landlord redirects past the
        // validate token and check user is landlord steps
        val joinOrganisationPage =
            navigator.goToAcceptOrganisationalLandlordInvitationJourneyWithValidToken(validToken)
        assertPageIs(page, JoinOrganisationPage::class)
        assertEquals("Join your organisation", joinOrganisationPage.heading.getText())
        assertEquals(
            true,
            joinOrganisationPage.introductionText.textContent()?.contains("Local Organisation Landlord."),
        )
        joinOrganisationPage.form.submit()

        // 2. Full Name page
        val fullNamePage = assertPageIs(page, FullNamePage::class)
        fullNamePage.submitName("Jane Smith")

        // 3. Email Address page
        val emailAddressPage = assertPageIs(page, EmailAddressPage::class)
        emailAddressPage.submitEmail("invitee@example.com")

        // 4. Submit Check Answers
        val checkAnswersPage = assertPageIs(page, CheckAnswersPage::class)
        checkAnswersPage.form.submit()

        // 5. Confirmation page
        val confirmationPage = assertPageIs(page, ConfirmationPage::class)
        assertTrue(confirmationPage.heading.getText().contains("TODO PDJB-1775"))
    }

    @Test
    fun `Invitees are redirected to the invalid link page when their invitation has expired`(page: Page) {
        // Go to the start of accept invitation journey with an expired invitation token
        val invalidLinkPage =
            navigator.goToAcceptOrganisationalLandlordInvitationJourneyWithInvalidToken(expiredToken)

        assertPageIs(page, InvalidLinkPage::class)
        BaseComponent.assertThat(invalidLinkPage.heading)
            .containsText("There was a problem with this invitation link")
        BaseComponent.assertThat(invalidLinkPage.signInLink).hasAttribute("href", LANDLORD_DASHBOARD_URL)
    }

    @Test
    fun `Invitees are redirected to the invalid link page when their invitation token is not recognised`(page: Page) {
        // Go to the start of accept invitation journey with a token that matches no invitation
        val invalidLinkPage =
            navigator.goToAcceptOrganisationalLandlordInvitationJourneyWithInvalidToken(unrecognisedToken)

        assertPageIs(page, InvalidLinkPage::class)
        BaseComponent.assertThat(invalidLinkPage.heading)
            .containsText("There was a problem with this invitation link")
    }

    @Test
    fun `Validate token page is unavailable when multi user organisations is disabled`(page: Page) {
        featureFlagManager.disable(MULTI_USER_ORGANISATIONS)

        val response = page.navigate("http://localhost:$port$ACCEPT_INVITATION_ROUTE?token=$validToken")
        assertEquals(404, response?.status())
    }

    @Nested
    inner class UserIsAlreadyAnIndividualLandlord : NestedIntegrationTestWithMutableData("data-local.sql") {
        @Test
        fun `Invitees who are already an individual landlord are shown the already landlord page`(page: Page) {
            // Act
            val userIsAlreadyLandlordPage =
                navigator.goToAcceptOrganisationalLandlordInvitationJourneyAsExistingLandlord(validToken)

            // Assert
            assertPageIs(page, UserIsAlreadyLandlordPage::class)
            BaseComponent
                .assertThat(userIsAlreadyLandlordPage.heading)
                .containsText("You need to use a different GOV.UK One Login to accept this invitation")
            BaseComponent.assertThat(userIsAlreadyLandlordPage.signOutLink).isVisible()

            // The invitation is untouched, so the correct account can still accept it
            val invitation = invitationRepository.findByToken(UUID.fromString(validToken))!!
            assertEquals(OrganisationalLandlordInvitationStatus.PENDING, invitation.status)
        }
    }

    @Nested
    inner class UserIsAlreadyAnOrganisationalLandlordUser : NestedIntegrationTestWithMutableData(
        listOf(
            "data-local.sql",
            "data-org-invitation-mockuser-not-landlord.sql",
            "data-org-invitation-mockuser-is-org-landlord-user.sql",
        ),
    ) {
        @Test
        fun `Invitees who are already an organisational landlord user are shown the already landlord page`(page: Page) {
            // Act
            val userIsAlreadyLandlordPage =
                navigator.goToAcceptOrganisationalLandlordInvitationJourneyAsExistingLandlord(validToken)

            // Assert
            assertPageIs(page, UserIsAlreadyLandlordPage::class)
            BaseComponent
                .assertThat(userIsAlreadyLandlordPage.heading)
                .containsText("You need to use a different GOV.UK One Login to accept this invitation")
        }
    }
}
