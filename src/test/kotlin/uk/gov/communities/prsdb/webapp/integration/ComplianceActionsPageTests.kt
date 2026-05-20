package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_INFO_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.USE_COMPLIANCE_ACTIONS_PAGE_REDESIGN
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithImmutableData.NestedIntegrationTestWithImmutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import java.util.regex.Pattern
import kotlin.test.Test
import kotlin.test.assertEquals

class ComplianceActionsPageTests : IntegrationTest() {
    @Nested
    inner class LandlordsWithComplianceActions :
        NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-compliance-actions.sql") {
        @BeforeEach
        fun disableRedesignFlag() {
            featureFlagManager.disableFeature(USE_COMPLIANCE_ACTIONS_PAGE_REDESIGN)
        }

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

        @Test
        fun `summary cards do not show occupied or unoccupied tags when redesign feature flag is disabled`() {
            val complianceActionsPage = navigator.goToComplianceActions()
            val occupiedCard = complianceActionsPage.getSummaryCard("4 Pretend Crescent")
            assertThat(occupiedCard).not().containsText("Occupied")
            val unoccupiedCard = complianceActionsPage.getSummaryCard("5 Invented Lane")
            assertThat(unoccupiedCard).not().containsText("Unoccupied")
        }
    }

    @Nested
    inner class LandlordsWithoutComplianceActions : NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-properties.sql") {
        @Test
        fun `the page loads with heading and inset text`() {
            val complianceActionsPage = navigator.goToComplianceActions()
            assertThat(complianceActionsPage.heading).containsText("Compliance actions")
            assertThat(complianceActionsPage.hintText).isHidden()
            assertThat(complianceActionsPage.insetText).containsText("The certificates for your occupied properties are up to date.")
        }
    }

    @Nested
    inner class RedesignedPageWithComplianceActions :
        NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-compliance-actions.sql") {
        @BeforeEach
        fun enableRedesignFlag() {
            featureFlagManager.enableFeature(USE_COMPLIANCE_ACTIONS_PAGE_REDESIGN)
        }

        @Test
        fun `the redesigned page loads with heading and body text`() {
            val complianceActionsPage = navigator.goToComplianceActions()
            assertThat(complianceActionsPage.heading).containsText("Compliance actions")
            assertThat(complianceActionsPage.bodyText).containsText("Add certificates to these properties.")
        }

        @Test
        fun `the redesigned page does not show old hint text`() {
            val complianceActionsPage = navigator.goToComplianceActions()
            assertThat(complianceActionsPage.hintText).isHidden()
        }

        @Test
        fun `redesigned summary cards are populated with correct content and actions`(page: Page) {
            var complianceActionsPage = navigator.goToComplianceActions()
            // Check compliance card - OCCUPIED, gas missing, eicr exempt, epc expired
            val completedComplianceCard = complianceActionsPage.getRedesignedSummaryCard("4 Pretend Crescent")
            assertThat(completedComplianceCard.summaryList.registrationNumRow).containsText("P-CCCT-GRKC")
            assertThat(completedComplianceCard.summaryList.gasSafetyRow).containsText("Not added")
            assertThat(completedComplianceCard.summaryList.electricalSafetyRow).isHidden()
            assertThat(completedComplianceCard.summaryList.energyPerformanceRow).containsText("Expired")

            completedComplianceCard.getAction("Go to property").link.clickAndWait()
            var propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to "3"))
            assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)

            // Check compliance card - UNOCCUPIED, gas missing, eicr exempt, epc expired
            complianceActionsPage = navigator.goToComplianceActions()
            val secondComplianceCard = complianceActionsPage.getRedesignedSummaryCard("5 Invented Lane")
            assertThat(secondComplianceCard.summaryList.registrationNumRow).containsText("P-CCCT-GRKF")
            assertThat(secondComplianceCard.summaryList.gasSafetyRow).isHidden()
            assertThat(secondComplianceCard.summaryList.electricalSafetyRow).isHidden()
            assertThat(secondComplianceCard.summaryList.energyPerformanceRow).containsText("Expired")

            secondComplianceCard.getAction("Go to property").link.clickAndWait()
            propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to "4"))
            assertEquals(COMPLIANCE_INFO_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)

            // Check compliance card - OCCUPIED, gas valid, eicr missing, epc in date but low rating
            complianceActionsPage = navigator.goToComplianceActions()
            val thirdComplianceCard = complianceActionsPage.getRedesignedSummaryCard("2 Fake Way")
            assertThat(thirdComplianceCard.summaryList.registrationNumRow).containsText("P-CCCT-GRJ5")
            assertThat(thirdComplianceCard.summaryList.gasSafetyRow).isHidden()
            assertThat(thirdComplianceCard.summaryList.electricalSafetyRow).containsText("Not added")
            assertThat(thirdComplianceCard.summaryList.energyPerformanceRow).containsText("Not added")

            // Check compliance card - UNOCCUPIED, gas valid, eicr missing, epc low rating
            complianceActionsPage = navigator.goToComplianceActions()
            assertThat(complianceActionsPage.getRedesignedSummaryCard("3 Imaginary Street")).isHidden()
        }

        @Test
        fun `redesigned summary cards show occupied status tag for occupied properties`() {
            val complianceActionsPage = navigator.goToComplianceActions()
            val occupiedCard = complianceActionsPage.getRedesignedSummaryCard("4 Pretend Crescent")
            assertThat(occupiedCard.summaryList.statusRow).containsText("Occupied")
            val tag =
                occupiedCard.summaryList.statusRow.value
                    .locator(".govuk-tag")
            PlaywrightAssertions.assertThat(tag).isVisible()
            PlaywrightAssertions.assertThat(tag).hasClass(Pattern.compile(".*govuk-tag--pink.*"))
        }

        @Test
        fun `redesigned summary cards show unoccupied status tag for unoccupied properties`() {
            val complianceActionsPage = navigator.goToComplianceActions()
            val unoccupiedCard = complianceActionsPage.getRedesignedSummaryCard("5 Invented Lane")
            assertThat(unoccupiedCard.summaryList.statusRow).containsText("Unoccupied")
            val tag =
                unoccupiedCard.summaryList.statusRow.value
                    .locator(".govuk-tag")
            PlaywrightAssertions.assertThat(tag).isVisible()
            PlaywrightAssertions.assertThat(tag).hasClass(Pattern.compile(".*govuk-tag--grey.*"))
        }
    }

    @Nested
    inner class StatusRowNotShownWhenFlagDisabled :
        NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-compliance-actions.sql")
}
