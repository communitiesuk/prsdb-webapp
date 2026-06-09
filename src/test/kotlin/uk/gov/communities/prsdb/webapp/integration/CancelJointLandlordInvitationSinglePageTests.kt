package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelJointLandlordInvitationJourneyPages.AreYouSurePageCancelJointLandlordInvitation
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater

class CancelJointLandlordInvitationSinglePageTests : IntegrationTestWithImmutableData("data-joint-landlord-invitation.sql") {
    private val pendingInvitationId = 3L

    @BeforeEach
    fun enableJointLandlordsFlag() {
        FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)
    }

    @Test
    fun `submitting without selecting an option shows a validation error`(page: Page) {
        val areYouSurePage = navigator.goToCancelJointLandlordInvitationAreYouSurePage(pendingInvitationId)
        areYouSurePage.form.submit()
        assertPageIs(page, AreYouSurePageCancelJointLandlordInvitation::class)
        assertThat(areYouSurePage.form.getErrorMessage("wantsToProceed"))
            .containsText("Select if you want to cancel this invitation")
    }
}
