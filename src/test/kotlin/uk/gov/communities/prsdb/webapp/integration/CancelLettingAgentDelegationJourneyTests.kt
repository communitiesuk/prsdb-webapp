package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.controllers.CancelLettingAgentDelegationController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelLettingAgentDelegationJourneyPages.AreYouSurePageCancelLettingAgentDelegation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelLettingAgentDelegationJourneyPages.ConfirmationPageCancelLettingAgentDelegation
import kotlin.test.assertEquals

class CancelLettingAgentDelegationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val propertyOwnershipId = 1L
    private val lettingAgentEmail = "letting.agent.one@example.com"

    @Test
    fun `a landlord can walk the remove letting agent journey and reach the confirmation page`(page: Page) {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)

        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.removeLettingAgentLink.clickAndWait()

        // "Are you sure" page
        val areYouSurePage = assertPageIs(page, AreYouSurePageCancelLettingAgentDelegation::class)
        BaseComponent.assertThat(areYouSurePage.form.fieldsetHeading)
            .containsText("Are you sure you want to remove $lettingAgentEmail?")
        assertThat(page.locator("main")).containsText(
            "They will no longer be able to provide details for this property record",
        )
        areYouSurePage.submitWantsToProceed()

        // Confirmation page
        val confirmationPage = assertPageIs(page, ConfirmationPageCancelLettingAgentDelegation::class)
        BaseComponent.assertThat(confirmationPage.confirmationBanner).containsText(
            "Letting agent or property manager can no longer provide details",
        )
        // TODO: PDJB-1560: Make sure the email is shown on this page
        confirmationPage.continueButton.clickAndWait()

        // Back to the property record page
        assertPageIs(
            page,
            PropertyDetailsPageLandlordView::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )

        // The delegation has been removed, so the journey can no longer be started
        val response =
            navigator.navigate(
                CancelLettingAgentDelegationController.getRemoveLettingAgentPath(propertyOwnershipId),
            )

        assertEquals(404, response?.status())
    }

    @Test
    fun `a landlord who selects no is returned to the property record with the letting agent still delegated`(page: Page) {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)

        val areYouSurePage = navigator.goToCancelLettingAgentDelegationAreYouSurePage(propertyOwnershipId)
        areYouSurePage.submitDoesNotWantToProceed()

        assertPageIs(
            page,
            PropertyDetailsPageLandlordView::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )

        // The delegation is untouched, so the journey can be started again
        val restartedPage = navigator.goToCancelLettingAgentDelegationAreYouSurePage(propertyOwnershipId)
        BaseComponent.assertThat(restartedPage.form.fieldsetHeading)
            .containsText("Are you sure you want to remove $lettingAgentEmail?")
    }

    @Test
    fun `the remove letting agent journey is unavailable when the flag is disabled`() {
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)

        val response =
            navigator.navigate(
                CancelLettingAgentDelegationController.getRemoveLettingAgentPath(propertyOwnershipId),
            )

        assertEquals(404, response?.status())

        val confirmationResponse =
            navigator.navigate(
                CancelLettingAgentDelegationController.getRemoveLettingAgentConfirmationPath(propertyOwnershipId),
            )

        assertEquals(404, confirmationResponse?.status())
    }
}
