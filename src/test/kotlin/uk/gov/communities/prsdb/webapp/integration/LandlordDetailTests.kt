package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import kotlin.test.assertEquals

@Sql("/data-local.sql")
class LandlordDetailTests : IntegrationTest() {
    @Test
    fun `loading the landlord details page shows personal details`(page: Page) {
        val detailsPage = navigator.goToLandlordDetails()

        assertEquals(detailsPage.getActiveTabPanelId(), "personal-details")
    }

    @Test
    fun `loading the landlord details page and selecting properties shows the registered properties`(page: Page) {
        val detailsPage = navigator.goToLandlordDetails()

        detailsPage.goToRegisteredProperties()

        assertEquals(detailsPage.getActiveTabPanelId(), "registered-properties")
    }
}
