package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
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
    fun `Page displays the correct heading and inviter details`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectJointLandlordInvitationJourney(validToken)
        assertThat(acceptOrRejectPage.heading).containsText("Tell us if you're a joint landlord for a property")
    }

    @Test
    fun `Selecting Yes redirects to accepted confirmation`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectJointLandlordInvitationJourney(validToken)
        acceptOrRejectPage.radios.selectValue("true")
        acceptOrRejectPage.form.submit()
        assertPageIs(page, PropertyJoinedConfirmationPage::class)
    }

    @Test
    fun `Selecting No redirects to rejection confirmation`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectJointLandlordInvitationJourney(validToken)
        acceptOrRejectPage.radios.selectValue("false")
        acceptOrRejectPage.form.submit()
        assertPageIs(page, InvitationRejectedConfirmationPage::class)
    }

    @Test
    fun `Submitting without selection shows validation error`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectJointLandlordInvitationJourney(validToken)
        acceptOrRejectPage.form.submit()
        assertThat(acceptOrRejectPage.heading).containsText("Tell us if you're a joint landlord for a property")
        page.locator(".govuk-error-summary").isVisible
    }
}
