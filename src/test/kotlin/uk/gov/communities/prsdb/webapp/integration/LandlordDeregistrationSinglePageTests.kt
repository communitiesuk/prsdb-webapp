package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat as playwrightAssertThat
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
            areYouSurePage.withPropertiesCancelLink.clickAndWait()
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
        fun `User is returned to the landlord details page if they submit No`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.submitDoesNotWantToProceed()
            BasePage.assertPageIs(page, LandlordDetailsPage::class)
        }

        @Test
        fun `User is returned to the landlord details page if they click the cancel link`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.noPropertiesCancelLink.clickAndWait()
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
            areYouSurePage.noPropertiesForm.submit()
            playwrightAssertThat(areYouSurePage.noPropertiesForm.getErrorMessage("wantsToProceed"))
                .containsText("Select whether you want to delete your account")
        }
        
        @Test
        fun `Are you sure page shows radio buttons without with-properties bullet content`() {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            val content = areYouSurePage.page.content()

            assertThat(areYouSurePage.noPropertiesForm.fieldsetHeading).isVisible()
            assertThat(areYouSurePage.noPropertiesContinueButton).isVisible()
            assertThat(areYouSurePage.noPropertiesCancelLink).isVisible()
            assertThat(areYouSurePage.withPropertiesYesDeleteButton).isHidden()
            assertThat(areYouSurePage.withPropertiesCancelLink).isHidden()
            assertFalse(content.contains("You’ll no longer be registered as a landlord in England."))
        }
    }
}
