package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.AreYouSureFormPagePropertyDeregistration
import java.net.URI
import kotlin.test.assertEquals

class PropertyDetailsTests : SinglePageTestWithSeedData("data-local.sql") {
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
            Assertions.assertEquals(LandlordDetailsController.LANDLORD_DETAILS_ROUTE, URI(page.url()).path)
        }

        @Test
        fun `in the landlord details section the landlord name link goes the landlord view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.getLandlordLinkFromLandlordDetails("Alexander Smith").clickAndWait()

            assertPageIs(page, LandlordDetailsPage::class)
            Assertions.assertEquals(LandlordDetailsController.LANDLORD_DETAILS_ROUTE, URI(page.url()).path)
        }

        @Test
        fun `the back link returns to the dashboard`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLandlordView(1)
            detailsPage.backLink.clickAndWait()
            assertPageIs(page, LandlordDashboardPage::class)
        }

        @Test
        fun `the delete button redirects to the delete record page`(page: Page) {
            val propertyOwnershipId = 1
            val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId.toLong())
            detailsPage.deleteButton.clickAndWait()
            assertPageIs(
                page,
                AreYouSureFormPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
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
            Assertions.assertEquals("${LandlordDetailsController.LANDLORD_DETAILS_ROUTE}/1", URI(page.url()).path)
        }

        @Test
        fun `in the landlord details section the landlord name link goes the local authority view of landlord details`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.tabs.goToLandlordDetails()

            detailsPage.getLandlordLinkFromLandlordDetails("Alexander Smith").clickAndWait()

            assertPageIs(page, LocalAuthorityViewLandlordDetailsPage::class)
            Assertions.assertEquals("${LandlordDetailsController.LANDLORD_DETAILS_ROUTE}/1", URI(page.url()).path)
        }

        @Test
        fun `the back link returns to the dashboard`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)
            detailsPage.backLink.clickAndWait()
            assertPageIs(page, LocalAuthorityDashboardPage::class)
        }

        @Test
        fun `loading the landlord details page shows the last time the landlords record was updated`(page: Page) {
            val detailsPage = navigator.goToPropertyDetailsLocalAuthorityView(1)

            assertThat(detailsPage.insetText).containsText("updated these details on")
        }
    }
}
