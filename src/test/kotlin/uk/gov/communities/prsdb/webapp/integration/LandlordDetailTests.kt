package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import kotlin.test.assertEquals

@Sql("/data-local.sql")
class LandlordDetailTests : IntegrationTest() {
    @Nested
    inner class LandlordDetailsView {
        @Test
        fun `the landlord details page loads with the landlords personal details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToLandlordDetails()

            assertEquals(detailsPage.getActiveTabPanelId(), "personal-details")
        }

        @Test
        fun `loading the landlord details page and selecting properties shows the registered properties table`(page: Page) {
            val detailsPage = navigator.goToLandlordDetails()

            detailsPage.goToRegisteredProperties()

            assertEquals(detailsPage.getActiveTabPanelId(), "registered-properties")
            assertThat(detailsPage.table.headerRow.getCell(0)).containsText("Property address")
            assertThat(detailsPage.table.headerRow.getCell(1)).containsText("Local authority")
            assertThat(detailsPage.table.headerRow.getCell(2)).containsText("Property licence")
            assertThat(detailsPage.table.headerRow.getCell(3)).containsText("Tenanted")
        }
    }

    @Nested
    inner class LandlordDetailsLocalAuthorityView {
        @Test
        fun `the landlord details page loads with the landlords personal details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalAuthorityUser(1)

            assertEquals(detailsPage.getActiveTabPanelId(), "personal-details")
        }

        @Test
        fun `loading the landlord details page and selecting properties shows landlord's registered properties table`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalAuthorityUser(1)

            detailsPage.goToRegisteredProperties()

            assertEquals(detailsPage.getActiveTabPanelId(), "registered-properties")
            assertThat(detailsPage.table.headerRow.getCell(0)).containsText("Property address")
            assertThat(detailsPage.table.headerRow.getCell(1)).containsText("Registration number")
            assertThat(detailsPage.table.headerRow.getCell(2)).containsText("Local authority")
            assertThat(detailsPage.table.headerRow.getCell(3)).containsText("Licensing type")
            assertThat(detailsPage.table.headerRow.getCell(4)).containsText("Tenanted")
        }

        @Test
        fun `loading the landlord details page shows the last time the landlords record was updated`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalAuthorityUser(1)

            assertThat(detailsPage.insetText.spanText).containsText("updated these details on")
        }
    }
}
