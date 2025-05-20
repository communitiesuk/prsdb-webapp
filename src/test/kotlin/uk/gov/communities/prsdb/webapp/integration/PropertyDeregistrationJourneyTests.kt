package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ConfirmationPagePropertyDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ReasonPagePropertyDeregistration

class PropertyDeregistrationJourneyTests : JourneyTestWithSeedData("data-local.sql") {
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
    inner class ReasonStep {
        @Test
        fun `Reason page can be submitted without being filled in`(page: Page) {
            val propertyOwnershipId = 1.toLong()
            val deregisterPropertyReasonPage = navigator.skipToPropertyDeregistrationReasonPage(propertyOwnershipId)
            deregisterPropertyReasonPage.form.submit()
            assertPageIs(
                page,
                ConfirmationPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }
    }
}
