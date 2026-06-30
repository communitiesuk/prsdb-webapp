package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelJointLandlordInvitationJourneyPages.AreYouSurePageCancelJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelJointLandlordInvitationJourneyPages.ConfirmationPageCancelJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater

class CancelJointLandlordInvitationJourneyTests :
    IntegrationTestWithMutableData("data-joint-landlord-invitation.sql") {
    private val propertyOwnershipId = 2L

    @BeforeEach
    fun enableJointLandlordsFlag() {
        FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)
    }

    @Test
    fun `selecting no on the cancel invitation page returns to property record with invitation still visible`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        detailsPage.tabs.goToLandlordDetails()
        detailsPage.pendingInvitationsSummary.click()
        detailsPage.cancelInvitationLink.clickAndWait()

        val areYouSurePage = assertPageIs(page, AreYouSurePageCancelJointLandlordInvitation::class)
        assertThat(page.locator("main")).containsText("pending@example.com")
        areYouSurePage.submitDoesNotWantToProceed()

        var propertyDetailsPage =
            assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        propertyDetailsPage.tabs.goToLandlordDetails()
        assertThat(propertyDetailsPage.pendingInvitationsDetails).isVisible()
        assertThat(propertyDetailsPage.pendingInvitationsDetails).containsText("pending@example.com")
    }

    @Test
    fun `selecting yes on the cancel invitation page cancels the invite and shows confirmation`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        detailsPage.tabs.goToLandlordDetails()
        detailsPage.pendingInvitationsSummary.click()
        detailsPage.cancelInvitationLink.clickAndWait()

        val areYouSurePage = assertPageIs(page, AreYouSurePageCancelJointLandlordInvitation::class)
        assertThat(page.locator("main")).containsText("pending@example.com")
        areYouSurePage.submitWantsToProceed()

        val confirmationPage = assertPageIs(page, ConfirmationPageCancelJointLandlordInvitation::class)
        assertThat(confirmationPage.confirmationBanner).containsText("pending@example.com")
        confirmationPage.goBackToPropertyRecordLink.clickAndWait()

        var propertyDetailsPage =
            assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        propertyDetailsPage.tabs.goToLandlordDetails()
        assertThat(propertyDetailsPage.pendingInvitationsDetails).hasCount(0)
    }
}
