package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages.KeepPropertySafePagePropertyComplianceUpdate
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PropertyComplianceUpdateSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    @Test
    fun `User can review their property safety declaration`(page: Page) {
        // Go to property compliance tab of property record
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID)
        propertyDetailsPage.tabs.goToComplianceInformation()

        // Review property safety declaration
        propertyDetailsPage.propertyComplianceSummaryList.propertySafetyRow.clickActionLinkAndWait()
        val reviewPropertySafetyPage = assertPageIs(page, KeepPropertySafePagePropertyComplianceUpdate::class, urlArguments)
        assertContains(reviewPropertySafetyPage.heading.getText(), "Keeping this property safe")

        // Go back to property record
        reviewPropertySafetyPage.returnToRecordButton.clickAndWait()
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 8L

        private val urlArguments = mapOf("propertyOwnershipId" to PROPERTY_OWNERSHIP_ID.toString())
    }
}
