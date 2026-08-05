package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelJointLandlordInvitationJourneyPages.AreYouSurePageCancelJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelJointLandlordInvitationJourneyPages.ConfirmationPageCancelJointLandlordInvitation

@WithOrgLandlordProfile
class JointLandlordInvitationOrgLandlordJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val propertyOwnershipId = 48L
    private val invitedEmail = "jl.pending.org@example.com"

    @Test
    fun `An org landlord can cancel a pending invitation on their property`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        detailsPage.tabs.goToLandlordDetails()
        detailsPage.pendingInvitationsSummary.click()
        detailsPage.cancelInvitationLink.clickAndWait()

        val areYouSurePage = assertPageIs(page, AreYouSurePageCancelJointLandlordInvitation::class)
        assertThat(page.locator("main")).containsText(invitedEmail)
        areYouSurePage.submitWantsToProceed()

        val confirmationPage = assertPageIs(page, ConfirmationPageCancelJointLandlordInvitation::class)
        assertThat(confirmationPage.confirmationBanner).containsText(invitedEmail)
        confirmationPage.goBackToPropertyRecordLink.clickAndWait()

        val propertyDetailsPage =
            assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        propertyDetailsPage.tabs.goToLandlordDetails()
        assertThat(propertyDetailsPage.pendingInvitationsDetails).hasCount(0)
    }

    @Test
    fun `An org landlord can resend a pending invitation on their property`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        detailsPage.tabs.goToLandlordDetails()
        detailsPage.pendingInvitationsSummary.click()
        detailsPage.resendInvitationLink.clickAndWait()

        val propertyDetailsPage =
            assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        assertThat(page.locator("main")).containsText("New invitation email sent to $invitedEmail")
    }
}
