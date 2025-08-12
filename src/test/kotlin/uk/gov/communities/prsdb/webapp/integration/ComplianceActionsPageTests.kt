package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithImmutableData.NestedIntegrationTestWithImmutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.StartPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.TaskListPagePropertyCompliance
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
            // Check not started compliance form
            var complianceActionsPage = navigator.goToComplianceActions()
            val notStartedComplianceCard = complianceActionsPage.getSummaryCard("3 Imaginary Street")
            assertThat(notStartedComplianceCard.summaryList.registrationNumRow).containsText("P-C5YY-J34H")
            assertThat(notStartedComplianceCard.summaryList.gasSafetyRow).containsText("Not started")
            assertThat(notStartedComplianceCard.summaryList.electricalSafetyRow).containsText("Not started")
            assertThat(notStartedComplianceCard.summaryList.energyPerformanceRow).containsText("Not started")

            notStartedComplianceCard.getAction("Start").link.clickAndWait()
            assertPageIs(page, StartPagePropertyCompliance::class, mapOf("propertyOwnershipId" to "2"))

            // Check in progress compliance form
            complianceActionsPage = navigator.goToComplianceActions()
            val inProgressComplianceCard = complianceActionsPage.getSummaryCard("2 Fake Way")
            assertThat(inProgressComplianceCard.summaryList.registrationNumRow).containsText("P-CCCT-GRJ5")
            assertThat(inProgressComplianceCard.summaryList.gasSafetyRow).containsText("Expired")
            assertThat(inProgressComplianceCard.summaryList.electricalSafetyRow).containsText("Added")
            assertThat(inProgressComplianceCard.summaryList.energyPerformanceRow).containsText("Not added")

            inProgressComplianceCard.getAction("Continue").link.clickAndWait()
            assertPageIs(page, TaskListPagePropertyCompliance::class, mapOf("propertyOwnershipId" to "1"))

            // Check completed compliance form
            complianceActionsPage = navigator.goToComplianceActions()
            val completedComplianceCard = complianceActionsPage.getSummaryCard("4 Pretend Crescent")
            assertThat(completedComplianceCard.summaryList.registrationNumRow).containsText("P-CCCT-GRKC")
            assertThat(completedComplianceCard.summaryList.gasSafetyRow).containsText("Not added")
            assertThat(completedComplianceCard.summaryList.electricalSafetyRow).isHidden()
            assertThat(completedComplianceCard.summaryList.energyPerformanceRow).containsText("Expired")

            completedComplianceCard.getAction("Update expired or missing certificates").link.clickAndWait()
            val propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to "3"))
            assertEquals(propertyDetailsPage.tabs.activeTabPanelId, COMPLIANCE_INFO_FRAGMENT)
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
