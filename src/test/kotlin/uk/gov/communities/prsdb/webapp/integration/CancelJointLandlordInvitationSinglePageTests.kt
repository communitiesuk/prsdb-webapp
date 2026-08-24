package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelJointLandlordInvitationJourneyPages.AreYouSurePageCancelJointLandlordInvitation

class CancelJointLandlordInvitationSinglePageTests : IntegrationTestWithImmutableData("data-joint-landlord-invitation.sql") {
    private val pendingInvitationId = 3L

    @Test
    fun `submitting without selecting an option shows a validation error`(page: Page) {
        val areYouSurePage = navigator.goToCancelJointLandlordInvitationAreYouSurePage(pendingInvitationId)
        areYouSurePage.form.submit()
        assertPageIs(page, AreYouSurePageCancelJointLandlordInvitation::class)
        assertThat(areYouSurePage.form.getErrorMessage("wantsToProceed"))
            .containsText("Select if you want to cancel this invitation")
    }

    @Test
    fun `the page has a Continue button and a cancel link`() {
        val areYouSurePage = navigator.goToCancelJointLandlordInvitationAreYouSurePage(pendingInvitationId)

        BaseComponent.assertThat(areYouSurePage.form.submitButton).containsText("Continue")
        BaseComponent.assertThat(areYouSurePage.cancelLink).isVisible()
    }
}
