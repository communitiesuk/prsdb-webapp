package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.InvitationRejectedConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.PropertyJoinedConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class AcceptOrRejectJointLandlordInvitationJourneyTests : IntegrationTestWithMutableData("data-joint-landlord-invitation.sql") {
    private val validToken = "aaaabbbb-cccc-dddd-eeee-ffff00001111"

    @BeforeEach
    fun enableJointLandlordsFlag() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
    }

    @Test
    fun `User with a valid token can accept the invitation and reach a confirmation page`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectJointLandlordInvitationJourney(validToken)
        // TODO PDJB-260 - check that the inviting landlord name appears on the page
        // TODO PDJB-260 - check that the property address appears on the page
        acceptOrRejectPage.acceptInvitation()

        // TODO PDJB-260 - if the user is already logged in as a registered landlord this should work.
        //  If not logged in, they should log in then get to the confirmation page
        //  If not a landlord, they need to register before they reach the confirmation page
        assertPageIs(page, PropertyJoinedConfirmationPage::class)
    }

    @Test
    fun `User with a valid token can reject the invitation and reach a confirmation page`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectJointLandlordInvitationJourney(validToken)
        // TODO PDJB-260 - add tests for the invite being rejected
        //  Add tests checking that unauthenticated users are asked to log in / register before reaching the confirmation page
        acceptOrRejectPage.rejectInvitation()
        assertPageIs(page, InvitationRejectedConfirmationPage::class)
    }
}
