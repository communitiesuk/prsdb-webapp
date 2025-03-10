package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ReasonPagePropertyDeregistration

@Sql("/data-local.sql")
class PropertyDeregistrationJourneyTests : IntegrationTest() {
    @Test
    fun `User can navigate the whole journey if pages are correctly filled in`(page: Page) {
        val deregisterPropertyAreYouSurePage = navigator.goToPropertyDeregistrationAreYouSurePage(1.toLong())
        assertThat(deregisterPropertyAreYouSurePage.form.fieldsetHeading).containsText("1, Example Road, EG")
        deregisterPropertyAreYouSurePage.submitWantsToProceed()
        val reasonPage = assertPageIs(page, ReasonPagePropertyDeregistration::class)

        // TODO: PRSD-697 - add the reason step

        // TOOD: : PRSD-698 - add the confirmation page
    }

    @Nested
    inner class AreYouSureStep {
        @Test
        fun `User is returned to the property details page if they select No`(page: Page) {
            val deregisterPropertyAreYouSurePage = navigator.goToPropertyDeregistrationAreYouSurePage(1.toLong())
            deregisterPropertyAreYouSurePage.submitDoesNotWantToProceed()
            // TODO: PRSD_696 - can we check that the same propertyOwnershipId appears in the property details url?
            assertPageIs(page, PropertyDetailsPageLandlordView::class)
        }

        @Test
        fun `User is returned to the property details page if they click the back link`(page: Page) {
            val deregisterPropertyAreYouSurePage = navigator.goToPropertyDeregistrationAreYouSurePage(1.toLong())
            deregisterPropertyAreYouSurePage.backLink.clickAndWait()
            // TODO: PRSD_696 - can we check that the same propertyOwnershipId appears in the property details url?
            assertPageIs(page, PropertyDetailsPageLandlordView::class)
        }

        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val deregisterPropertyAreYouSurePage = navigator.goToPropertyDeregistrationAreYouSurePage(1.toLong())
            deregisterPropertyAreYouSurePage.form.submit()
            assertThat(deregisterPropertyAreYouSurePage.form.getErrorMessage("wantsToProceed"))
                .containsText("Select whether you want to delete this property from the database")
        }
    }
}
