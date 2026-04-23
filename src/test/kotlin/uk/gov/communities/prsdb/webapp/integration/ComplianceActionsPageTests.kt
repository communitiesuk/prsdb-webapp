package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithImmutableData.NestedIntegrationTestWithImmutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.Test
import kotlin.test.assertEquals

class ComplianceActionsPageTests : IntegrationTest() {
    @Nested
    inner class LandlordsWithComplianceActions :
        NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-compliance-actions.sql") {
        @Test
        fun `the page loads with heading and subheading`() {
            val complianceActionsPage = navigator.goToComplianceActions()
            assertThat(complianceActionsPage.heading).containsText("Compliance actions")
            assertThat(complianceActionsPage.hintText).containsText("This is a list of properties missing some compliance information.")
        }

        @Test
        fun `Summary cards are populated with correct content and actions`(page: Page) {
            var complianceActionsPage = navigator.goToComplianceActions()
            // Check completed compliance form - OCCUPIED, gas missing, eicr exempt, epc expired
            val completedComplianceCard = complianceActionsPage.getSummaryCard("4 Pretend Crescent")
            assertThat(completedComplianceCard.summaryList.registrationNumRow).containsText("P-CCCT-GRKC")
            assertThat(completedComplianceCard.summaryList.gasSafetyRow).containsText("Not added")
            assertThat(completedComplianceCard.summaryList.electricalSafetyRow).isHidden()
            assertThat(completedComplianceCard.summaryList.energyPerformanceRow).containsText("Expired")

            completedComplianceCard.getAction("Go to property").link.clickAndWait()
            var propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to "3"))
            assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)

            // Check completed compliance form - UNOCCUPIED, gas missing, eicr exempt, epc expired
            complianceActionsPage = navigator.goToComplianceActions()
            val secondCompleteActionsCard = complianceActionsPage.getSummaryCard("5 Invented Lane")
            assertThat(secondCompleteActionsCard.summaryList.registrationNumRow).containsText("P-CCCT-GRKF")
            assertThat(secondCompleteActionsCard.summaryList.gasSafetyRow).isHidden()
            assertThat(secondCompleteActionsCard.summaryList.electricalSafetyRow).isHidden()
            assertThat(secondCompleteActionsCard.summaryList.energyPerformanceRow).containsText("Expired")

            secondCompleteActionsCard.getAction("Go to property").link.clickAndWait()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to "4"))
            assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)

            // Check completed compliance form - OCCUPIED, gas valid, eicr missing, epc in date but low rating
            complianceActionsPage = navigator.goToComplianceActions()
            val thirdComplianceCard = complianceActionsPage.getSummaryCard("2 Fake Way")
            assertThat(thirdComplianceCard.summaryList.registrationNumRow).containsText("P-CCCT-GRJ5")
            assertThat(thirdComplianceCard.summaryList.gasSafetyRow).isHidden()
            assertThat(thirdComplianceCard.summaryList.electricalSafetyRow).containsText("Not added")
            assertThat(thirdComplianceCard.summaryList.energyPerformanceRow).containsText("Not added")

            // Check completed compliance form - UNOCCUPIED, gas valid, eicr missing, epc low rating
            complianceActionsPage = navigator.goToComplianceActions()
            assertThat(complianceActionsPage.getSummaryCard("3 Imaginary Street")).isHidden()
        }
    }

    @Nested
    inner class LandlordsWithoutComplianceActions : NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-properties.sql") {
        @Test
        fun `the page loads with heading and page text`() {
            val complianceActionsPage = navigator.goToComplianceActions()
            assertThat(complianceActionsPage.heading).containsText("Compliance actions")
            assertThat(complianceActionsPage.hintText).containsText("You have no properties awaiting compliance information")
        }

        @Test
        fun `the view registered properties link goes to the property records tab on the landlord details page`(page: Page) {
            val complianceActionsPage = navigator.goToComplianceActions()
            complianceActionsPage.viewRegisteredPropertiesLink.clickAndWait()
            val detailsPage = assertPageIs(page, LandlordDetailsPage::class)
            assertEquals("registered-properties", detailsPage.tabs.activeTabPanelId)
        }
    }
}
