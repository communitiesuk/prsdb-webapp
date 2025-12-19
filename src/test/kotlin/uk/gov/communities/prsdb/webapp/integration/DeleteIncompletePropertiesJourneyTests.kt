package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteIncompletePropertyRegistrationAreYouSurePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteIncompletePropertyRegistrationConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordIncompletePropertiesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class DeleteIncompletePropertiesJourneyTests : IntegrationTestWithMutableData("data-mockuser-landlord-with-one-incomplete-property.sql") {
    val contextId = "1"
    val singleLineAddress = "1, SAVOY COURT, LONDON, WC2R 0EX"

    @Test
    fun `The user can delete an incomplete property registration`(page: Page) {
        // Incomplete properties page - with incomplete property
        var incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
        assertThat(incompletePropertiesPage.heading).containsText("Incomplete property details")
        assertThat(
            incompletePropertiesPage.subHeading,
        ).containsText("Complete the missing details for these properties. After 28 days, incomplete properties are deleted.")
        incompletePropertiesPage.firstSummaryCard.deleteLink.clickAndWait()
        val areYouSurePage = assertPageIs(page, DeleteIncompletePropertyRegistrationAreYouSurePage::class, mapOf("contextId" to contextId))

        // Are you sure page
        assertThat(areYouSurePage.heading).containsText("Are you sure you want to delete $singleLineAddress from the database?")
        areYouSurePage.submitWantsToProceed()
        val confirmationPage =
            assertPageIs(
                page,
                DeleteIncompletePropertyRegistrationConfirmationPage::class,
                mapOf("contextId" to contextId),
            )

        // Confirmation page
        confirmationPage.returnToIncompleteProperties.clickAndWait()
        incompletePropertiesPage = assertPageIs(page, LandlordIncompletePropertiesPage::class)

        // Updated incomplete properties page - no incomplete properties
        assertThat(incompletePropertiesPage.heading).containsText("Incomplete property details")
        assertThat(incompletePropertiesPage.subHeading).containsText("You have no properties with missing or incomplete details.")
        assertThat(incompletePropertiesPage.text).containsText("You can either view registered properties or register a new property.")
    }
}
