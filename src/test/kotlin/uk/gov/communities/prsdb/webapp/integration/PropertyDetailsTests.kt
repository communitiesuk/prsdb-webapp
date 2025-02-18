package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
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

            assertEquals(detailsPage.tabs.activeTabPanelId, "property-details")
        }

        @Test
        fun `loading the landlord details page and clicking landlord details tab shows the landlords details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, "landlord-details")
        }

        @Test
        fun `when the landlord details tab is active clicking the property details tab shows property details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.tabs.goToPropertyDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, "property-details")
        }

        @Test
        fun `in the key details section the landlord name link goes the landlord view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.getLandlordNameLinkFromKeyDetails("Alexander Smith").clickAndWait()

            assertPageIs(page, LandlordDetailsPage::class)
            Assertions.assertEquals("/landlord-details", URI(page.url()).path)
        }

        @Test
        fun `in the landlord details section the landlord name link goes the landlord view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.getLandlordLinkFromLandlordDetails("Alexander Smith").clickAndWait()

            assertPageIs(page, LandlordDetailsPage::class)
            Assertions.assertEquals("/landlord-details", URI(page.url()).path)
        }

        @Test
        fun `the back link returns to the dashboard`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.backLink.clickAndWait()

            // TODO: PRSD-647 add link to the dashboard
            Assertions.assertEquals("/property-details/1", URI(page.url()).path)
        }

        @Test
        fun `the delete button redirects to the delete record page`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.deleteButton.clickAndWait()

            Assertions.assertEquals("/property-details/delete-record", URI(page.url()).path)
        }
    }

    @Nested
    inner class PropertyDetailsLocalAuthorityView {
        @Test
        fun `the property details page loads with the property details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)

            assertEquals(detailsPage.tabs.activeTabPanelId, "property-details")
        }

        @Test
        fun `loading the landlord details page and clicking landlord details tab shows the landlords details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.tabs.goToLandlordDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, "landlord-details")
        }

        @Test
        fun `when the landlord details tab is active clicking the property details tab shows property details tab`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.tabs.goToPropertyDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, "property-details")
        }

        @Test
        fun `in the key details section the landlord name link goes the local authority view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.getLandlordNameLinkFromKeyDetails("Alexander Smith").clickAndWait()

            assertPageIs(page, LocalAuthorityViewLandlordDetailsPage::class)
            Assertions.assertEquals("/landlord-details/1", URI(page.url()).path)
        }

        @Test
        fun `in the landlord details section the landlord name link goes the local authority view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.getLandlordLinkFromLandlordDetails("Alexander Smith").clickAndWait()

            assertPageIs(page, LocalAuthorityViewLandlordDetailsPage::class)
            Assertions.assertEquals("/landlord-details/1", URI(page.url()).path)
        }

        @Test
        fun `the back link returns to the dashboard`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.backLink.clickAndWait()

            // TODO: PRSD-647 add link to the dashboard
            Assertions.assertEquals("/local-authority/property-details/1", URI(page.url()).path)
        }

        @Test
        fun `loading the landlord details page shows the last time the landlords record was updated`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)

            assertThat(detailsPage.insetText.spanText).containsText("updated these details on")
        }
    }
}
