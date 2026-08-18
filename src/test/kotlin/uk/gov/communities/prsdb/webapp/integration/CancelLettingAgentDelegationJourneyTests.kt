package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLettingAgentDelegationConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class CancelLettingAgentDelegationJourneyTests : IntegrationTestWithImmutableData("data-local.sql") {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Test
    fun `a landlord can walk the remove letting agent journey and reach the confirmation page`(page: Page) {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)

        // TODO PDJB-1412: enter the journey via the letting agent panel button on the property record,
        //  once the MOJ ticket panel has been added, instead of navigating directly

        // "Are you sure" page
        val areYouSurePage = navigator.goToCancelLettingAgentDelegationAreYouSurePage(propertyOwnershipId)
        // TODO PDJB-1413: assert the real "are you sure" page content and the yes/no decision
        areYouSurePage.continueButton.clickAndWait()

        // Confirmation page (terminal)
        val confirmationPage = assertPageIs(page, CancelLettingAgentDelegationConfirmationPage::class, urlArguments)
        // TODO PDJB-1413: assert the real confirmation page content and add the onward link back to the property record
        BaseComponent.assertThat(confirmationPage.confirmationBanner).containsText("TODO")
    }

    @Test
    fun `the remove letting agent journey is unavailable when the flag is disabled`() {
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)
        // TODO PDJB-1413: assert the are-you-sure and confirmation endpoints return 404 when the flag is disabled
    }
}
