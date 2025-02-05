package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import java.net.URI
import kotlin.test.assertEquals

@Sql("/data-local.sql")
class PropertyDetailsTests : IntegrationTest() {
    @Nested
    inner class PropertyDetailsLandlordView {
        @Test
        fun `the property details page loads with the property details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)

            assertEquals(detailsPage.getActiveTabPanelId(), "property-details")
        }

        @Test
        fun `loading the landlord details page and clicking landlord details tab shows the landlords details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.goToLandlordDetails()

            assertEquals(detailsPage.getActiveTabPanelId(), "landlord-details")
        }

        @Test
        fun `when the landlord details tab is active clicking the property details tab shows property details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            println(detailsPage.page.content())
            detailsPage.goToLandlordDetails()

            detailsPage.goToPropertyDetails()

            assertEquals(detailsPage.getActiveTabPanelId(), "property-details")
        }

        @Test
        fun `the landlord name link goes the landlord view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.clickLandlordNameLink("Alexander Smith")

            assertPageIs(page, LandlordDetailsPage::class)
            Assertions.assertEquals("/landlord-details", URI(page.url()).path)
        }
    }

    @Nested
    inner class PropertyDetailsLocalAuthorityView {
        @Test
        fun `the property details page loads with the property details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)

            assertEquals(detailsPage.getActiveTabPanelId(), "property-details")
        }

        @Test
        fun `loading the landlord details page and clicking landlord details tab shows the landlords details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.goToLandlordDetails()

            assertEquals(detailsPage.getActiveTabPanelId(), "landlord-details")
        }

        @Test
        fun `when the landlord details tab is active clicking the property details tab shows property details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            println(detailsPage.page.content())
            detailsPage.goToLandlordDetails()

            detailsPage.goToPropertyDetails()

            assertEquals(detailsPage.getActiveTabPanelId(), "property-details")
        }

        @Test
        fun `the landlord name link goes the landlord view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.clickLandlordNameLink("Alexander Smith")

            assertPageIs(page, LocalAuthorityViewLandlordDetailsPage::class)
            Assertions.assertEquals("/landlord-details/1", URI(page.url()).path)
        }
    }
}
