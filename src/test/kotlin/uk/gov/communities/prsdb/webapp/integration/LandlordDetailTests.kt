package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalAuthorityView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import java.net.URI
import kotlin.test.assertEquals

@Sql("/data-local.sql")
class LandlordDetailTests : IntegrationTest() {
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
            assertThat(detailsPage.table.headerRow.getCell(0)).containsText("Property address")
            assertThat(detailsPage.table.headerRow.getCell(1)).containsText("Local authority")
            assertThat(detailsPage.table.headerRow.getCell(2)).containsText("Property licence")
            assertThat(detailsPage.table.headerRow.getCell(3)).containsText("Tenanted")
        }

        @Test
        fun `in the registered properties table the property address link goes to the landlord view of the property's details`(page: Page) {
            val detailsPage = navigator.goToLandlordDetails()
            detailsPage.tabs.goToRegisteredProperties()

            detailsPage.getPropertyAddressLink("1, Example Road, EG").clickAndWait()

            assertPageIs(page, PropertyDetailsPageLandlordView::class)
            Assertions.assertEquals(
                PropertyDetailsController.getPropertyDetailsPath(1, isLaView = false),
                URI(page.url()).path,
            )
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
            assertThat(detailsPage.table.headerRow.getCell(0)).containsText("Property address")
            assertThat(detailsPage.table.headerRow.getCell(1)).containsText("Registration number")
            assertThat(detailsPage.table.headerRow.getCell(2)).containsText("Local authority")
            assertThat(detailsPage.table.headerRow.getCell(3)).containsText("Licensing type")
            assertThat(detailsPage.table.headerRow.getCell(4)).containsText("Tenanted")
        }

        @Test
        fun `loading the landlord details page shows the last time the landlords record was updated`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalAuthorityUser(1)

            assertThat(detailsPage.insetText).containsText("updated these details on")
        }

        @Test
        fun `in the registered properties table the property address link goes to the LA view of the property's details`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalAuthorityUser(1)
            detailsPage.tabs.goToRegisteredProperties()

            detailsPage.getPropertyAddressLink("1, Example Road, EG").clickAndWait()

            assertPageIs(page, PropertyDetailsPageLocalAuthorityView::class)
            Assertions.assertEquals(
                PropertyDetailsController.getPropertyDetailsPath(1, isLaView = true),
                URI(page.url()).path,
            )
        }
    }
}
