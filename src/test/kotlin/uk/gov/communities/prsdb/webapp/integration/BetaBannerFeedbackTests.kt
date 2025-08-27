package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.betaFeedbackPages.BetaFeedbackPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.betaFeedbackPages.LandlordBetaFeedbackSuccessPage
import kotlin.test.Test

class BetaBannerFeedbackTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `Give your feedback link from a landlord's beta banner goes to the feedback page`(page: Page) {
        val dashboard = navigator.goToLandlordDashboard()
        val popupPromise =
            page.waitForPopup {
                dashboard.betaBanner.giveFeedbackLink.clickAndWait()
            }
        val feedbackPage = assertPageIs(popupPromise, BetaFeedbackPage::class)
        // On the deployed service, an AWS cloudfront function will insert the "/landlord" prefix to hit the LANDLORD_FEEDBACK_URL endpoint

        assertThat(feedbackPage.form.referrerInput).hasValue("http://localhost:$port/landlord/dashboard?continue")
    }

    @Test
    fun `Give your feedback link from a lc user's beta banner goes to the feedback page`(page: Page) {
        val dashboard = navigator.goToLocalAuthorityDashboard()
        val popupPromise =
            page.waitForPopup {
                dashboard.betaBanner.giveFeedbackLink.clickAndWait()
            }
        val feedbackPage = assertPageIs(popupPromise, BetaFeedbackPage::class)
        // On the deployed service, an AWS cloudfront function will insert the "/local-council" prefix to hit the LOCAL_AUTHORITY_FEEDBACK_URL endpoint

        assertThat(feedbackPage.form.referrerInput).hasValue("http://localhost:$port/local-council/dashboard?continue")
    }

    @Nested
    inner class LandlordBetaFeedbackPage {
        @Test
        fun `submitting with an empty feedback field gives a validation error`(page: Page) {
            val feedbackPage = navigator.goToLandlordBetaFeedbackPage()
            feedbackPage.form.submit()
            assertThat(feedbackPage.form.getErrorMessage("feedback"))
                .containsText("Enter your feedback about the service")
        }

        @Test
        fun `submitting with too much text in the feedback field gives a validation error`(page: Page) {
            val feedbackPage = navigator.goToLandlordBetaFeedbackPage()
            feedbackPage.form.feedbackInput.fill("This will be too long".repeat(120))
            feedbackPage.form.submit()
            assertThat(feedbackPage.form.getErrorMessage("feedback"))
                .containsText("Your feedback must be 1,200 characters or fewer")
        }

        @Test
        fun `submitting an invalid gives a validation error`(page: Page) {
            val feedbackPage = navigator.goToLandlordBetaFeedbackPage()
            feedbackPage.form.emailInput.fill("not-an-email")
            feedbackPage.form.submit()
            assertThat(feedbackPage.form.getErrorMessage("email"))
                .containsText("Enter a valid email address")
        }

        @Test
        fun `submitting with feedback but no email address redirects to the success page`(page: Page) {
            val feedbackPage = navigator.goToLandlordBetaFeedbackPage()
            feedbackPage.form.feedbackInput.fill("This is my feedback")
            feedbackPage.form.submit()
            assertPageIs(page, LandlordBetaFeedbackSuccessPage::class)
        }

        @Test
        fun `submitting with feedback and a email address redirects to the success page`(page: Page) {
            val feedbackPage = navigator.goToLandlordBetaFeedbackPage()
            feedbackPage.form.feedbackInput.fill("This is my feedback")
            feedbackPage.form.emailInput.fill("email.address@example.com")
            feedbackPage.form.submit()
            assertPageIs(page, LandlordBetaFeedbackSuccessPage::class)
        }
    }
}
//
