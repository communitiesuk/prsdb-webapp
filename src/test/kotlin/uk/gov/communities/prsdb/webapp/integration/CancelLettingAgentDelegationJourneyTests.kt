package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLettingAgentDelegationAreYouSurePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLettingAgentDelegationConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class CancelLettingAgentDelegationJourneyTests : IntegrationTestWithImmutableData("data-local.sql") {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Test
    fun `a landlord can walk the remove letting agent journey from the property record and back again`(page: Page) {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)

        // Property record page
        val propertyRecord = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        // TODO PDJB-1412: assert the letting agent panel content and "Remove your letting agent or property manager" button
        propertyRecord.removeLettingAgentButton.clickAndWait()

        // "Are you sure" page
        val areYouSurePage = assertPageIs(page, CancelLettingAgentDelegationAreYouSurePage::class, urlArguments)
        // TODO PDJB-1413: assert the real "are you sure" page content and the yes/no decision
        areYouSurePage.continueButton.clickAndWait()

        // Confirmation page
        val confirmationPage = assertPageIs(page, CancelLettingAgentDelegationConfirmationPage::class, urlArguments)
        // TODO PDJB-1413: assert the confirmation page content
        confirmationPage.continueButton.clickAndWait()

        // Back to the property record page
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
    }

    @Test
    fun `the remove letting agent journey is unavailable when the flag is disabled`() {
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)
        // TODO PDJB-1412: assert the letting agent panel and button are hidden on the property record
        // TODO PDJB-1413: assert the are-you-sure and confirmation endpoints return 404 when the flag is disabled
    }
}
