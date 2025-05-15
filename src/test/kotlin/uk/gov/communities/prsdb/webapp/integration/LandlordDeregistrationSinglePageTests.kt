package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.testHelpers.SqlBeforeAll

class LandlordDeregistrationSinglePageTests : IntegrationTest() {
    @SqlBeforeAll("/data-mockuser-landlord-with-properties.sql")
    @Nested
    inner class LandlordWithProperties : NestedTestWithSeedData() {
        @Test
        fun `User is returned to the landlord details page if they submit No`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.submitDoesNotWantToProceed()

            BasePage.assertPageIs(page, LandlordDetailsPage::class)
        }

        @Test
        fun `User is returned to the landlord details page if they click the back link`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.backLink.clickAndWait()
            BasePage.assertPageIs(page, LandlordDetailsPage::class)
        }

        @Test
        fun `Submitting with no option selected returns an error`() {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.form.submit()
            PlaywrightAssertions
                .assertThat(areYouSurePage.form.getErrorMessage("wantsToProceed"))
                .containsText("Select whether you want to delete your landlord record and properties")
        }
    }

    @SqlBeforeAll("/data-unverified-landlord.sql")
    @Nested
    inner class LandlordWithoutProperties : NestedTestWithSeedData() {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.form.submit()
            PlaywrightAssertions
                .assertThat(areYouSurePage.form.getErrorMessage("wantsToProceed"))
                .containsText("Select whether you want to delete your account from the database")
        }
    }
}
