package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalAuthorityView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals

class LandlordDetailTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class LandlordDetailsView {
        @Test
        fun `the landlord details page loads with the landlords personal details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToLandlordDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, "personal-details")
        }

        @Test
        fun `loading the landlord details page and selecting properties shows the registered properties table`(page: Page) {
            val detailsPage = navigator.goToLandlordDetails()

            detailsPage.tabs.goToRegisteredProperties()

            assertEquals(detailsPage.tabs.activeTabPanelId, "registered-properties")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(0)).containsText("Property address")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(1)).containsText("Local council")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(2)).containsText("Property licence")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(3)).containsText("Tenanted")
        }

        @Test
        fun `in the registered properties table the property address link goes to the landlord view of the property's details`(page: Page) {
            val propertyOwnershipId = 1
            val detailsPage = navigator.goToLandlordDetails()
            detailsPage.tabs.goToRegisteredProperties()

            detailsPage.getPropertyAddressLink("1, Example Road, EG").clickAndWait()

            val propertyDetailsView =
                assertPageIs(
                    page,
                    PropertyDetailsPageLandlordView::class,
                    mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
                )

            propertyDetailsView.backLink.clickAndWait()
            assertPageIs(page, LandlordDetailsPage::class)
        }
    }

    @Nested
    inner class LandlordDetailsLocalAuthorityView {
        @Test
        fun `the landlord details page loads with the landlords personal details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalAuthorityUser(1)

            assertEquals(detailsPage.tabs.activeTabPanelId, "personal-details")
        }

        @Test
        fun `loading the landlord details page and selecting properties shows landlord's registered properties table`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalAuthorityUser(1)

            detailsPage.tabs.goToRegisteredProperties()

            assertEquals(detailsPage.tabs.activeTabPanelId, "registered-properties")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(0)).containsText("Property address")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(1)).containsText("Registration number")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(2)).containsText("Local council")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(3)).containsText("Licensing type")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(4)).containsText("Tenanted")
        }

        @Test
        fun `loading the landlord details page shows the last time the landlords record was updated`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalAuthorityUser(1)

            assertThat(detailsPage.insetText).containsText("updated these details on")
        }

        @Test
        fun `in the registered properties table the property address link goes to the LA view of the property's details`(page: Page) {
            val propertyOwnershipId = 1
            val detailsPage = navigator.goToLandlordDetailsAsALocalAuthorityUser(propertyOwnershipId.toLong())
            detailsPage.tabs.goToRegisteredProperties()

            detailsPage.getPropertyAddressLink("1, Example Road, EG").clickAndWait()

            val propertyDetailsView =
                assertPageIs(
                    page,
                    PropertyDetailsPageLocalAuthorityView::class,
                    mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
                )

            propertyDetailsView.backLink.clickAndWait()
            assertPageIs(page, LocalAuthorityViewLandlordDetailsPage::class, mapOf("id" to propertyOwnershipId.toString()))
        }
    }
}
