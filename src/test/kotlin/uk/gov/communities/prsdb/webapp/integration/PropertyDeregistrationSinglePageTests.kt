package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PropertyDeregistrationSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class InfoStep {
        @Test
        fun `Page displays correct heading with property address`(page: Page) {
            val propertyOwnershipId = 1
            val deregisterPropertyInfoPage = navigator.goToDeregisterPropertyInfoPage(propertyOwnershipId.toLong())
            assertThat(deregisterPropertyInfoPage.heading).containsText("Deregister")
            assertThat(deregisterPropertyInfoPage.heading).containsText("1, Example Road, EG")
        }

        @Test
        fun `Page displays PRN information`(page: Page) {
            val deregisterPropertyInfoPage = navigator.goToDeregisterPropertyInfoPage(1.toLong())
            assertThat(deregisterPropertyInfoPage.prnHeading).containsText("What happens to the Property Registration Number")
        }

        @Test
        fun `User is returned to the property details page if they click the cancel link`(page: Page) {
            val propertyOwnershipId = 1
            val deregisterPropertyInfoPage = navigator.goToDeregisterPropertyInfoPage(propertyOwnershipId.toLong())
            deregisterPropertyInfoPage.cancelLink.clickAndWait()
            BasePage.assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }

        @Test
        fun `User is returned to the property details page if they click the back link`(page: Page) {
            val propertyOwnershipId = 1
            val deregisterPropertyInfoPage = navigator.goToDeregisterPropertyInfoPage(propertyOwnershipId.toLong())
            deregisterPropertyInfoPage.backLink.clickAndWait()
            BasePage.assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
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
            assertThat(deregisterPropertyReasonPage.form.getErrorMessage("reason"))
                .containsText("Your reason for deleting this property must be 200 characters or fewer")
        }
    }
}
