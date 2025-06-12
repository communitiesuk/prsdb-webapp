package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.LocatorAssertions
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.SinglePageTestWithSeedData.NestedSinglePageTestWithSeedData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.StartPagePropertyCompliance
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.TaskListPagePropertyCompliance
import kotlin.test.Test
import kotlin.test.assertEquals

class LandlordIncompleteCompliancesPageTests : IntegrationTest() {
    @Nested
    inner class LandlordsWithIncompleteCompliances :
        NestedSinglePageTestWithSeedData("data-mockuser-landlord-with-incomplete-compliances.sql") {
        @Test
        fun `the page loads with heading and subheading`() {
            val incompleteCompliancesPage = navigator.goToLandlordIncompleteCompliances()
            assertThat(incompleteCompliancesPage.heading).containsText("Properties without compliance information")
            assertThat(
                incompleteCompliancesPage.subHeading,
            ).containsText("You must add compliance certificates for each property and confirm your landlord responsibilities")
        }

        @Test
        fun `Summary card titles are named and numbered correctly`(page: Page) {
            val incompleteCompliancesPage = navigator.goToLandlordIncompleteCompliances()
            assertThat(incompleteCompliancesPage.firstSummaryCard.title).containsText("Property 1")
            assertThat(incompleteCompliancesPage.secondSummaryCard.title).containsText("Property 2")
        }

        @Test
        fun `Summary card lists are populated correctly`() {
            val currentDate = DateTimeHelper().getCurrentDateInUK()
            val certificatesDueDate = currentDate.plus(DatePeriod(days = 28))
            val formattedCertificatesDueDate =
                "${certificatesDueDate.dayOfMonth} ${certificatesDueDate.month.name} ${certificatesDueDate.year}"

            val incompleteCompliancesPage = navigator.goToLandlordIncompleteCompliances()

            assertThat(incompleteCompliancesPage.firstSummaryCard.summaryCardList.propertyAddressRow).containsText("2 Fake Way")
            assertThat(incompleteCompliancesPage.firstSummaryCard.summaryCardList.localAuthorityRow).containsText("ISLE OF MAN")
            assertThat(
                incompleteCompliancesPage.firstSummaryCard.summaryCardList.certificatesDueRow,
            ).containsText(formattedCertificatesDueDate, LocatorAssertions.ContainsTextOptions().setIgnoreCase(true))
            assertThat(incompleteCompliancesPage.firstSummaryCard.summaryCardList.gasSafetyRow).containsText("Added")
            assertThat(incompleteCompliancesPage.firstSummaryCard.summaryCardList.electricalSafetyRow).containsText("Added")
            assertThat(incompleteCompliancesPage.firstSummaryCard.summaryCardList.energyPerformanceRow).containsText("Not added")
            assertThat(incompleteCompliancesPage.firstSummaryCard.summaryCardList.landlordResponsibilitiesRow).containsText("Not added")

            assertThat(incompleteCompliancesPage.secondSummaryCard.summaryCardList.propertyAddressRow).containsText("3 Imaginary Street")
            assertThat(
                incompleteCompliancesPage.secondSummaryCard.summaryCardList.localAuthorityRow,
            ).containsText("ISLE OF MAN")
            assertThat(
                incompleteCompliancesPage.secondSummaryCard.summaryCardList.certificatesDueRow,
            ).containsText(formattedCertificatesDueDate, LocatorAssertions.ContainsTextOptions().setIgnoreCase(true))
            assertThat(incompleteCompliancesPage.secondSummaryCard.summaryCardList.gasSafetyRow).containsText("Not added")
            assertThat(incompleteCompliancesPage.secondSummaryCard.summaryCardList.electricalSafetyRow).containsText("Not added")
            assertThat(incompleteCompliancesPage.secondSummaryCard.summaryCardList.energyPerformanceRow).containsText("Not added")
            assertThat(incompleteCompliancesPage.secondSummaryCard.summaryCardList.landlordResponsibilitiesRow).containsText("Not added")
        }

        @Test
        fun `Clicking on a summary card Continue link redirects to the task list page`(page: Page) {
            val incompleteCompliancesPage = navigator.goToLandlordIncompleteCompliances()
            incompleteCompliancesPage.firstSummaryCard.continueLink.clickAndWait()
            assertPageIs(page, TaskListPagePropertyCompliance::class, mapOf("propertyOwnershipId" to "1"))
        }

        @Test
        fun `Clicking on a summary card Start link redirects to the task list page`(page: Page) {
            val incompleteCompliancesPage = navigator.goToLandlordIncompleteCompliances()
            incompleteCompliancesPage.secondSummaryCard.startLink.clickAndWait()
            assertPageIs(page, StartPagePropertyCompliance::class, mapOf("propertyOwnershipId" to "2"))
        }
    }

    @Nested
    inner class LandlordsWithNoIncompleteCompliances :
        NestedSinglePageTestWithSeedData("data-mockuser-landlord-with-properties.sql") {
        @Test
        fun `the page loads with heading and page text`() {
            val incompleteCompliancesPage = navigator.goToLandlordIncompleteCompliances()
            assertThat(incompleteCompliancesPage.heading).containsText("Properties without compliance information")
            assertThat(incompleteCompliancesPage.hintText).containsText("You have no properties awaiting compliance information")
        }

        @Test
        fun `the view registered properties link goes to the property records tab on the landlord details page`(page: Page) {
            val incompleteCompliancesPage = navigator.goToLandlordIncompleteCompliances()
            incompleteCompliancesPage.viewRegisteredPropertiesLink.clickAndWait()
            val detailsPage = assertPageIs(page, LandlordDetailsPage::class)
            assertEquals("registered-properties", detailsPage.tabs.activeTabPanelId)
        }
    }
}
