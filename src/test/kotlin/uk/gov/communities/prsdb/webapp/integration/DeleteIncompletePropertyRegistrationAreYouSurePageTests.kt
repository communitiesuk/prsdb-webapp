package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordIncompletePropertiesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class DeleteIncompletePropertyRegistrationAreYouSurePageTests :
    IntegrationTestWithImmutableData("data-mockuser-landlord-with-one-incomplete-property.sql") {
    val contextId = "1"
    val singleLineAddress = "1, SAVOY COURT, LONDON, WC2R 0EX"

    @Test
    fun `the property is not deleted and the page redirects to the incomplete properties page if the user selects No`(page: Page) {
        val areYouSurePage = navigator.goToDeleteIncompletePropertyRegistrationAreYouSurePage(contextId)
        areYouSurePage.submitDoesNotWantToProceed()
        val incompletePropertiesPage = assertPageIs(page, LandlordIncompletePropertiesPage::class)
        BaseComponent.assertThat(incompletePropertiesPage.firstSummaryCard.title).containsText("Incomplete Property 1")
    }

    @Test
    fun `the page redirects to the incomplete properties page if the user clicks the back link`(page: Page) {
        val areYouSurePage = navigator.goToDeleteIncompletePropertyRegistrationAreYouSurePage(contextId)
        areYouSurePage.backLink.clickAndWait()
        assertPageIs(page, LandlordIncompletePropertiesPage::class)
    }

    @Test
    fun `the page loads with an error if the user clicks submit without selecting an option`() {
        val areYouSurePage = navigator.goToDeleteIncompletePropertyRegistrationAreYouSurePage(contextId)
        areYouSurePage.form.submit()
        assertThat(areYouSurePage.form.getErrorMessage("wantsToProceed"))
            .containsText("Select whether you want to delete this property from the database")
    }
}
