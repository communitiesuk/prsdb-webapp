package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

@Sql("/data-local.sql")
class PropertyDeregistrationSinglePageTests : IntegrationTest() {
    @Nested
    inner class AreYouSureStep {
        @Test
        fun `User is returned to the property details page if they select No`(page: Page) {
            val propertyOwnershipId = 1
            val deregisterPropertyAreYouSurePage = navigator.goToPropertyDeregistrationAreYouSurePage(propertyOwnershipId.toLong())
            deregisterPropertyAreYouSurePage.submitDoesNotWantToProceed()
            BasePage.assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }

        @Test
        fun `User is returned to the property details page if they click the back link`(page: Page) {
            val propertyOwnershipId = 1
            val deregisterPropertyAreYouSurePage = navigator.goToPropertyDeregistrationAreYouSurePage(1.toLong())
            deregisterPropertyAreYouSurePage.backLink.clickAndWait()
            BasePage.assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }

        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val deregisterPropertyAreYouSurePage = navigator.goToPropertyDeregistrationAreYouSurePage(1.toLong())
            deregisterPropertyAreYouSurePage.form.submit()
            PlaywrightAssertions
                .assertThat(deregisterPropertyAreYouSurePage.form.getErrorMessage("wantsToProceed"))
                .containsText("Select whether you want to delete this property from the database")
        }
    }

    @Nested
    inner class ReasonStep {
        @Test
        fun `Submitting with a reason longer than 200 characters returns an error`(page: Page) {
            val longReason =
                "This is my life story, it is far too long to go in this field.  " +
                    "This is my life story, it is far too long to go in this field." +
                    "This is my life story, it is far too long to go in this field." +
                    "This is my life story, it is far too long to go in this field."
            val deregisterPropertyReasonPage = navigator.skipToPropertyDeregistrationReasonPage(1.toLong())
            deregisterPropertyReasonPage.submitReason(longReason)
            PlaywrightAssertions
                .assertThat(deregisterPropertyReasonPage.form.getErrorMessage("reason"))
                .containsText("Your reason for deleting this property must be 200 characters or fewer")
        }
    }
}
