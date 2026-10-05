package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.CheckAnswersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.EmailAddressPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.FullNamePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.InvalidLinkPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.JoinOrganisationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.ValidateTokenPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.TokenValidity
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AcceptInvitationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(MULTI_USER_ORGANISATIONS)
    }

    @Test
    fun `Invitees can successfully accept an invitation to join an organisation`(page: Page) {
        // 1. Go to the start of accept invitation journey (Validate Token page)
        val validateTokenPage = navigator.goToAcceptInvitationJourney("1234abcd-5678-abcd-1234-567abcd2222a")
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
        emailAddressPage.form.submit()

        // 4. Check Answers page
        val checkAnswersPage = assertPageIs(page, CheckAnswersPage::class)
        checkAnswersPage.form.submit()

        // 5. Confirmation page
        val confirmationPage = assertPageIs(page, ConfirmationPage::class)
        assertTrue(confirmationPage.heading.getText().contains("TODO PDJB-1775"))
    }

    @Test
    fun `full name page shows the heading and button`() {
        val fullNamePage = navigator.goToAcceptInvitationFullNamePage()

        assertEquals("What is your full name?", fullNamePage.heading.getText().trim())
        BaseComponent.assertThat(fullNamePage.form.submitButton).isVisible()
    }

    @Test
    fun `pressing save without entering a name shows error`(page: Page) {
        val fullNamePage = navigator.goToAcceptInvitationFullNamePage()

        // Press Save without typing anything
        fullNamePage.form.submit()

        assertThat(fullNamePage.form.getErrorMessage()).containsText("You must enter your full name")
        assertPageIs(page, FullNamePage::class)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t"])
    fun `blank full names do not advance the journey`(
        blankName: String,
        page: Page,
    ) {
        val fullNamePage = navigator.goToAcceptInvitationFullNamePage()

        fullNamePage.submitName(blankName)
        assertThat(fullNamePage.form.getErrorMessage()).containsText("You must enter your full name")
        assertPageIs(page, FullNamePage::class)
    }

    @Test
    fun `Invitees are redirected to invalid link page if token is invalid`(page: Page) {
        // 1. Go to the start of accept invitation journey (Validate Token page)
        val validateTokenPage = navigator.goToAcceptInvitationJourney()
        assertPageIs(page, ValidateTokenPage::class)
        // TODO PDJB-1822: Validate token step
        validateTokenPage.form.radios.selectValue(TokenValidity.INVALID)
        validateTokenPage.form.submit()

        // 2. Invalid link page
        val invalidLinkPage = assertPageIs(page, InvalidLinkPage::class)
        assertTrue(invalidLinkPage.heading.getText().contains("TODO PDJB-1821)"))
    }

    @Test
    fun `Valid token choice with no invitation associated goes to invalid link page`(page: Page) {
        // 1. Go to the start of accept invitation journey (Validate Token page)
        val validateTokenPage = navigator.goToAcceptInvitationJourney()
        assertPageIs(page, ValidateTokenPage::class)
        // TODO PDJB-1822: Validate token step
        validateTokenPage.form.radios.selectValue(TokenValidity.VALID)
        validateTokenPage.form.submit()

        // 2. Invalid link page
        val invalidLinkPage = assertPageIs(page, InvalidLinkPage::class)
        assertTrue(invalidLinkPage.heading.getText().contains("TODO PDJB-1821)"))
    }
}
