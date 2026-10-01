package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.CheckAnswersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.EmailAddressPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.FullNamePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.InvalidLinkPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.JoinOrganisationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ValidateTokenPage
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.TokenValidity
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class AcceptInvitationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Test
    fun `Invitees can successfully accept an invitation to join an organisation`(page: Page) {
        featureFlagManager.enable(MULTI_USER_ORGANISATIONS)

        // 1. Go to the start of accept invitation journey (Validate Token page)
        val validateTokenPage = navigator.goToAcceptInvitationJourney()
        assertPageIs(page, ValidateTokenPage::class)
        // TODO PDJB-1822: Validate token step
        validateTokenPage.form.radios.selectValue(TokenValidity.VALID)
        validateTokenPage.form.submit()

        // 1b. Join Organisation page
        val joinOrganisationPage = assertPageIs(page, JoinOrganisationPage::class)
        joinOrganisationPage.form.submit()

        // 2. Full Name page
        val fullNamePage = assertPageIs(page, FullNamePage::class)
        fullNamePage.form.submit()

        // 3. Email Address page
        val emailAddressPage = assertPageIs(page, EmailAddressPage::class)
        emailAddressPage.form.submit()

        // 4. Check Answers page
        val checkAnswersPage = assertPageIs(page, CheckAnswersPage::class)
        checkAnswersPage.form.submit()

        // 5. Confirmation page
        val confirmationPage = assertPageIs(page, ConfirmationPage::class)
        BaseComponent.assertThat(confirmationPage.confirmationBanner).containsText("TODO")
    }

    @Test
    fun `Invitees are redirected to invalid link page if token is invalid`(page: Page) {
        featureFlagManager.enable(MULTI_USER_ORGANISATIONS)

        // 1. Go to the start of accept invitation journey (Validate Token page)
        val validateTokenPage = navigator.goToAcceptInvitationJourney()
        assertPageIs(page, ValidateTokenPage::class)
        // TODO PDJB-1822: Validate token step
        validateTokenPage.form.radios.selectValue(TokenValidity.INVALID)
        validateTokenPage.form.submit()

        // 2. Invalid link page
        val invalidLinkPage = assertPageIs(page, InvalidLinkPage::class)
        assertThat(invalidLinkPage.page.locator("button[type='submit'], button:has-text('Continue')")).hasCount(0)
    }
}
