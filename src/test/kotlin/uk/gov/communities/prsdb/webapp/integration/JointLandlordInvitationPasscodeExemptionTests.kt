package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages.AcceptOrRejectPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

@ActiveProfiles("require-passcode")
class JointLandlordInvitationPasscodeExemptionTests : IntegrationTestWithMutableData("data-joint-landlord-invitation.sql") {
    private val validToken = "aaaabbbb-cccc-dddd-eeee-ffff00001111"

    @BeforeEach
    fun enableJointLandlordsFlag() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
        featureFlagManager.disable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Test
    fun `Invited joint landlords can start the invitation journey without entering a passcode`(page: Page) {
        navigator.goToAcceptOrRejectValidJointLandlordInvitationJourney(validToken)
        assertPageIs(page, AcceptOrRejectPage::class)
    }
}
