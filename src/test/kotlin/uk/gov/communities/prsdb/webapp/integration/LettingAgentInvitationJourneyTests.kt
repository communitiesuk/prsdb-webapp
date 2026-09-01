package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.EnterPasswordPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.HasPasswordPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.InvalidLinkPageLettingAgentInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.PasswordCreationConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.SetPasswordPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages.StoreAccessPage

class LettingAgentInvitationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val validToken = "3334abcd-5678-abcd-1234-567abcd1111a"
    private val invalidToken = "00000000-0000-0000-0000-000000000000"

    @Test
    fun `user who does not have a password can walk the set password journey`(page: Page) {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)

        navigator.goToLettingAgentInvitationJourney(validToken)

        val hasPasswordPage = assertPageIs(page, HasPasswordPage::class)
        // TODO PDJB-1658: Remove this step from the journey test
        hasPasswordPage.submitNoPassword()

        // TODO PDJB-1566: Update when set password page is implemented
        val setPasswordPage = assertPageIs(page, SetPasswordPage::class)
        setPasswordPage.form.submit()

        // TODO PDJB-1567: Update when password creation confirmation page is implemented
        val confirmationPage = assertPageIs(page, PasswordCreationConfirmationPage::class)
        confirmationPage.form.submit()

        // TODO PDJB-1659: Remove this step from the journey test
        val storeAccessPage = assertPageIs(page, StoreAccessPage::class)
        storeAccessPage.form.submit()

        // TODO PDJB-1570: Assert redirect to letting agent property record page
    }

    @Test
    fun `user who has a password can walk the enter password journey`(page: Page) {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)

        navigator.goToLettingAgentInvitationJourney(validToken)

        val hasPasswordPage = assertPageIs(page, HasPasswordPage::class)
        // TODO PDJB-1658: Remove this step from the journey test
        hasPasswordPage.submitHasPassword()

        // TODO PDJB-1568: Update when enter password page is implemented
        val enterPasswordPage = assertPageIs(page, EnterPasswordPage::class)
        enterPasswordPage.form.submit()

        // TODO PDJB-1659: Remove this step from the journey test
        val storeAccessPage = assertPageIs(page, StoreAccessPage::class)
        storeAccessPage.form.submit()

        // TODO PDJB-1570: Assert redirect to letting agent property record page
    }

    @Test
    fun `user with an invalid token is redirected to the invalid link page`(page: Page) {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)

        navigator.goToLettingAgentInvitationJourney(invalidToken)

        assertPageIs(page, InvalidLinkPageLettingAgentInvitation::class)
    }
}
