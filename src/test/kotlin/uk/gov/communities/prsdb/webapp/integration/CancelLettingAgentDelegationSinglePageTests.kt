package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelLettingAgentDelegationJourneyPages.AreYouSurePageCancelLettingAgentDelegation

class CancelLettingAgentDelegationSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    private val propertyOwnershipId = 1L

    @BeforeEach
    fun enableFlag() {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)
    }

    @Test
    fun `submitting without selecting an option shows a validation error`(page: Page) {
        val areYouSurePage = navigator.goToCancelLettingAgentDelegationAreYouSurePage(propertyOwnershipId)

        areYouSurePage.form.submit()

        assertPageIs(page, AreYouSurePageCancelLettingAgentDelegation::class)
        assertThat(areYouSurePage.form.getErrorMessage("wantsToProceed"))
            .containsText("Select if you want to remove this letting agent or property manager")
    }

    @Test
    fun `the page has a Confirm button and no cancel link`() {
        val areYouSurePage = navigator.goToCancelLettingAgentDelegationAreYouSurePage(propertyOwnershipId)

        BaseComponent.assertThat(areYouSurePage.form.submitButton).containsText("Confirm")
        BaseComponent.assertThat(areYouSurePage.cancelLink).not().isVisible()
    }
}
