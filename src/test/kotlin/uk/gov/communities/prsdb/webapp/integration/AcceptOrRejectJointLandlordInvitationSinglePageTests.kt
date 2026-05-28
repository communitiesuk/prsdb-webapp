package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test

class AcceptOrRejectJointLandlordInvitationSinglePageTests : IntegrationTestWithImmutableData("data-joint-landlord-invitation.sql") {
    private val validToken = "aaaabbbb-cccc-dddd-eeee-ffff00001111"

    @Test
    fun `Submitting acceptOrReject page without selection shows validation error`(page: Page) {
        val acceptOrRejectPage = navigator.goToAcceptOrRejectJointLandlordInvitationJourney(validToken)
        acceptOrRejectPage.form.submit()
        assertThat(acceptOrRejectPage.form.getErrorMessage("isInviteAccepted"))
            .containsText("Select if you’re a landlord for this property")
    }
}
