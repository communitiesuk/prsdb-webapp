package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.BetaFeedbackPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.Test

class BetaBannerFeedbackTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `Give your feedback link from a landlord's beta banner goes to the feedback page`(page: Page) {
        val dashboard = navigator.goToLandlordDashboard()
        val popupPromise =
            page.waitForPopup {
                dashboard.betaBanner.giveFeedbackLink.clickAndWait()
            }
        assertPageIs(popupPromise, BetaFeedbackPage::class)
        // On the deployed service, an AWS cloudfront function will insert the "/landlord" prefix to hit the LANDLORD_FEEDBACK_URL endpoint
    }

    @Test
    fun `Give your feedback link from a lc user's beta banner goes to the feedback page`(page: Page) {
        val dashboard = navigator.goToLocalAuthorityDashboard()
        val popupPromise =
            page.waitForPopup {
                dashboard.betaBanner.giveFeedbackLink.clickAndWait()
            }
        assertPageIs(popupPromise, BetaFeedbackPage::class)
        // On the deployed service, an AWS cloudfront function will insert the "/local-council" prefix to hit the LANDLORD_FEEDBACK_URL endpoint
    }
}
//
