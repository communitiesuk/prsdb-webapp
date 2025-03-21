package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ConfirmationPagePropertyDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ReasonPagePropertyDeregistration

@Sql("/data-local.sql")
class PropertyDeregistrationJourneyTests : IntegrationTest() {
    @Test
    fun `User can navigate the whole journey if pages are correctly filled in`(page: Page) {
        val propertyOwnershipId = 1
        val deregisterPropertyAreYouSurePage = navigator.goToPropertyDeregistrationAreYouSurePage(propertyOwnershipId.toLong())
        assertThat(deregisterPropertyAreYouSurePage.form.fieldsetHeading).containsText("1, Example Road, EG")
        deregisterPropertyAreYouSurePage.submitWantsToProceed()

        val reasonPage =
            assertPageIs(
                page,
                ReasonPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        reasonPage.submitReason("No longer own this property")

        val confirmationPage =
            assertPageIs(
                page,
                ConfirmationPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        assertThat(confirmationPage.confirmationBanner).containsText("You have deleted a property")

        confirmationPage.goToDashboardButton.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Nested
    inner class AreYouSureStep {
        @Test
        fun `User is returned to the property details page if they select No`(page: Page) {
            val propertyOwnershipId = 1
            val deregisterPropertyAreYouSurePage = navigator.goToPropertyDeregistrationAreYouSurePage(propertyOwnershipId.toLong())
            deregisterPropertyAreYouSurePage.submitDoesNotWantToProceed()
            assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to propertyOwnershipId.toString()))
        }

        @Test
        fun `User is returned to the property details page if they click the back link`(page: Page) {
            val propertyOwnershipId = 1
            val deregisterPropertyAreYouSurePage = navigator.goToPropertyDeregistrationAreYouSurePage(1.toLong())
            deregisterPropertyAreYouSurePage.backLink.clickAndWait()
            assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to propertyOwnershipId.toString()))
        }

        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val deregisterPropertyAreYouSurePage = navigator.goToPropertyDeregistrationAreYouSurePage(1.toLong())
            deregisterPropertyAreYouSurePage.form.submit()
            assertThat(deregisterPropertyAreYouSurePage.form.getErrorMessage("wantsToProceed"))
                .containsText("Select whether you want to delete this property from the database")
        }
    }

    @Nested
    inner class ReasonStep {
        @Test
        fun `Reason page can be submitted without being filled in`(page: Page) {
            val propertyOwnershipId = 1.toLong()
            val deregisterPropertyReasonPage = navigator.goToPropertyDeregistrationReasonPage(propertyOwnershipId)
            deregisterPropertyReasonPage.form.submit()
            assertPageIs(
                page,
                ConfirmationPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }

        @Test
        fun `Submitting with a reason longer than 200 characters returns an error`(page: Page) {
            val longReason =
                "This is my life story, it is far too long to go in this field.  " +
                    "This is my life story, it is far too long to go in this field." +
                    "This is my life story, it is far too long to go in this field." +
                    "This is my life story, it is far too long to go in this field."
            val deregisterPropertyReasonPage = navigator.goToPropertyDeregistrationReasonPage(1.toLong())
            deregisterPropertyReasonPage.submitReason(longReason)
            assertThat(deregisterPropertyReasonPage.form.getErrorMessage("reason"))
                .containsText("Your reason for deleting this property must be 200 characters or fewer")
        }
    }
}
