package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.FireSafetyDeclarationPagePropertyComplianceReview
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.KeepPropertySafePagePropertyComplianceReview
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.ResponsibilityToTenantsPagePropertyComplianceReview
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PropertyComplianceReviewSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `User can review their fire safety declaration`(page: Page) {
        // Go to property compliance tab of property record
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()

        // Review fire safety declaration
        propertyDetailsPage.propertyComplianceSummaryList.fireSafetyRow.clickFirstActionLinkAndWait()
        val reviewFireSafetyPage = assertPageIs(page, FireSafetyDeclarationPagePropertyComplianceReview::class, urlArguments)
        assertContains(reviewFireSafetyPage.heading.getText(), "Fire safety in your property")

        // Go back to property record
        reviewFireSafetyPage.returnToRecordButton.clickAndWait()
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `User can review their property safety declaration`(page: Page) {
        // Go to property compliance tab of property record
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()

        // Review property safety declaration
        propertyDetailsPage.propertyComplianceSummaryList.propertySafetyRow.clickFirstActionLinkAndWait()
        val reviewPropertySafetyPage = assertPageIs(page, KeepPropertySafePagePropertyComplianceReview::class, urlArguments)
        assertContains(reviewPropertySafetyPage.heading.getText(), "Health and safety in rental properties")

        // Go back to property record
        reviewPropertySafetyPage.returnToRecordButton.clickAndWait()
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `User can review their responsibility to tenants declaration`(page: Page) {
        // Go to property compliance tab of property record
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()

        // Review responsibility to tenants declaration
        propertyDetailsPage.propertyComplianceSummaryList.responsibilityToTenantsRow.clickFirstActionLinkAndWait()
        val reviewResponsibilityToTenantsPage = assertPageIs(page, ResponsibilityToTenantsPagePropertyComplianceReview::class, urlArguments)
        assertContains(
            reviewResponsibilityToTenantsPage.heading.getText(),
            "Your responsibilities to your tenants",
        )

        // Go back to property record
        reviewResponsibilityToTenantsPage.returnToRecordButton.clickAndWait()
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 8L

        private val urlArguments = mapOf("propertyOwnershipId" to PROPERTY_OWNERSHIP_ID.toString())
    }
}
