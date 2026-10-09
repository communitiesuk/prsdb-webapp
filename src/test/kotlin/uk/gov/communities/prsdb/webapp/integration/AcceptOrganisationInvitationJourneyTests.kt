package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.CheckAnswersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.CheckUserIsLandlordPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.EmailAddressPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.FullNamePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.InvalidLinkPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.JoinOrganisationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AcceptOrganisationInvitationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(MULTI_USER_ORGANISATIONS)
    }

    @Test
    fun `Invitees can successfully accept an invitation to join an organisation`(page: Page) {
        // 1. Go to the start of accept invitation journey - a valid token redirects past the validate token step
        val checkUserIsLandlordPage =
            navigator.goToAcceptOrganisationalLandlordInvitationJourney("1234abcd-5678-abcd-1234-567abcd2222a")
        assertPageIs(page, CheckUserIsLandlordPage::class)
        checkUserIsLandlordPage.form.submit()

        // 1b. Join Organisation page
        val joinOrganisationPage = assertPageIs(page, JoinOrganisationPage::class)
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

        // 4. Check Answers page
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
            navigator.goToAcceptOrganisationalLandlordInvitationJourneyWithInvalidToken("1234abcd-5678-abcd-1234-567abcd2222d")

        assertPageIs(page, InvalidLinkPage::class)
        BaseComponent.assertThat(invalidLinkPage.heading)
            .containsText("There was a problem with this invitation link")
        BaseComponent.assertThat(invalidLinkPage.signInLink).hasAttribute("href", LANDLORD_DASHBOARD_URL)
    }

    @Test
    fun `Invitees are redirected to the invalid link page when their invitation token is not recognised`(page: Page) {
        // Go to the start of accept invitation journey with a token that matches no invitation
        val invalidLinkPage =
            navigator.goToAcceptOrganisationalLandlordInvitationJourneyWithInvalidToken("1234abcd-5678-abcd-1234-567abcd9999a")

        assertPageIs(page, InvalidLinkPage::class)
        BaseComponent.assertThat(invalidLinkPage.heading)
            .containsText("There was a problem with this invitation link")
    }
}
