package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.CheckAnswersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.EmailAddressPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.FullNamePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.InvalidLinkPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.JoinOrganisationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ValidateTokenPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.TokenValidity
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AcceptOrganisationInvitationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var invitationRepository: OrganisationalLandlordInvitationRepository

    @Autowired
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(MULTI_USER_ORGANISATIONS)
    }

    @Test
    fun `Invitees can successfully accept an invitation to join an organisation`(page: Page) {
        // 1. Go to the start of accept invitation journey (Validate Token page)
        val validateTokenPage = navigator.goToAcceptOrganisationalLandlordInvitationJourney("1234abcd-5678-abcd-1234-567abcd2222a")
        assertPageIs(page, ValidateTokenPage::class)
        // TODO PDJB-1822: Validate token step
        validateTokenPage.form.radios.selectValue(TokenValidity.VALID)
        validateTokenPage.form.submit()

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

        // 4. Submit Check Answers
        val checkAnswersPage = assertPageIs(page, CheckAnswersPage::class)
        checkAnswersPage.form.submit()

        // 5. Confirmation page
        val confirmationPage = assertPageIs(page, ConfirmationPage::class)
        assertTrue(confirmationPage.heading.getText().contains("TODO PDJB-1775"))
    }

    // TODO PDJB-1822: Update test once validate token step is in place
    @Test
    fun `Invitees are redirected to invalid link page when token validation placeholder is invalid`(page: Page) {
        // 1. Go to the start of accept invitation journey (Validate Token page)
        val validateTokenPage = navigator.goToAcceptOrganisationalLandlordInvitationJourney("1234abcd-5678-abcd-1234-567abcd2222a")
        assertPageIs(page, ValidateTokenPage::class)
        // TODO PDJB-1822: Validate token step
        validateTokenPage.form.radios.selectValue(TokenValidity.INVALID)
        validateTokenPage.form.submit()

        // 2. Invalid link page
        val invalidLinkPage = assertPageIs(page, InvalidLinkPage::class)
        assertTrue(invalidLinkPage.heading.getText().contains("TODO PDJB-1821)"))
    }

    @Test
    fun `Validate token page is unavailable when multi user organisations is disabled`(page: Page) {
        featureFlagManager.disable(MULTI_USER_ORGANISATIONS)

        val response = page.navigate("http://localhost:$port$ACCEPT_INVITATION_ROUTE?token=1234abcd-5678-abcd-1234-567abcd2222a")
        assertEquals(404, response?.status())
    }
}
