package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithImmutableData.NestedIntegrationTestWithImmutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import kotlin.test.assertFalse

class LandlordDeregistrationSinglePageTests : IntegrationTest() {
    @Nested
    inner class LandlordWithProperties :
        NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-properties-and-incomplete-property.sql") {
        @Test
        fun `User is returned to the landlord details page if they click the cancel link`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.cancelLink.clickAndWait()
            BasePage.assertPageIs(page, LandlordDetailsPage::class)
        }

        @Test
        fun `User is returned to the landlord details page if they click the back link`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.backLink.clickAndWait()
            BasePage.assertPageIs(page, LandlordDetailsPage::class)
        }
    }

    @Nested
    inner class LandlordWithoutProperties : NestedIntegrationTestWithImmutableData("data-unverified-landlord.sql") {
        @Test
        fun `User is returned to the landlord details page if they click the cancel link`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.cancelLink.clickAndWait()
            BasePage.assertPageIs(page, LandlordDetailsPage::class)
        }

        @Test
        fun `User is returned to the landlord details page if they click the back link`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.backLink.clickAndWait()
            BasePage.assertPageIs(page, LandlordDetailsPage::class)
        }

        // TODO PDJB-1249: Waiting for designers to confirm if the content
        // on this page for ind LL without properties should be updated
        @Test
        fun `Are you sure page shows heading and actions without with-properties bullet content`() {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            val content = areYouSurePage.page.content()

            assertThat(areYouSurePage.heading).isVisible()
            assertThat(areYouSurePage.yesDeleteButton).isVisible()
            assertThat(areYouSurePage.cancelLink).isVisible()
            assertFalse(content.contains("You’ll no longer be registered as a landlord in England."))
        }
    }
}
