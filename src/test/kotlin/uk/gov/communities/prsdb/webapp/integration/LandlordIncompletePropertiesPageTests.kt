package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.LocatorAssertions
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithImmutableData.NestedIntegrationTestWithImmutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteIncompletePropertyRegistrationAreYouSurePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordIncompletePropertiesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RegisterPropertyStartPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.TaskListPagePropertyRegistration
import kotlin.test.Test
import kotlin.test.assertEquals

class LandlordIncompletePropertiesPageTests : IntegrationTest() {
    @Nested
    inner class LandlordsWithIncompleteProperties :
        NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-incomplete-properties.sql") {
        @Test
        fun `the page loads with heading and subheading`() {
            val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
            assertThat(incompletePropertiesPage.heading).containsText("Your incomplete properties")
            assertThat(
                incompletePropertiesPage.subHeading,
            ).containsText("You have 28 days to complete a property registration or the property will be deleted from the database")
        }

        @Test
        fun `Summary card titles are named and numbered correctly`(page: Page) {
            val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
            assertThat(incompletePropertiesPage.firstSummaryCard.title).containsText("Incomplete Property 1")
            assertThat(incompletePropertiesPage.secondSummaryCard.title).containsText("Incomplete Property 2")
        }

        @Test
        fun `Summary card lists are populated correctly`() {
            val currentDate = DateTimeHelper().getCurrentDateInUK()
            val completeByDate = currentDate.plus(DatePeriod(days = 28))
            val formattedCompleteByDate = "${completeByDate.dayOfMonth} ${completeByDate.month.name} ${completeByDate.year}"

            val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()

            assertThat(incompletePropertiesPage.firstSummaryCard.summaryList.propertyAddressRow).containsText("4, Example Road, EG")
            assertThat(
                incompletePropertiesPage.firstSummaryCard.summaryList.localAuthorityRow,
            ).containsText("SOUTH GLOUCESTERSHIRE COUNCIL")
            assertThat(
                incompletePropertiesPage.firstSummaryCard.summaryList.completeByRow,
            ).containsText(formattedCompleteByDate, LocatorAssertions.ContainsTextOptions().setIgnoreCase(true))

            assertThat(incompletePropertiesPage.secondSummaryCard.summaryList.propertyAddressRow).containsText("5, Example Road, EG")
            assertThat(incompletePropertiesPage.secondSummaryCard.summaryList.localAuthorityRow).containsText("NORTH SOMERSET COUNCIL")
            assertThat(
                incompletePropertiesPage.secondSummaryCard.summaryList.completeByRow,
            ).containsText(formattedCompleteByDate, LocatorAssertions.ContainsTextOptions().setIgnoreCase(true))
        }

        @Test
        fun `Clicking on a summary card Continue link redirects to the task list page`(page: Page) {
            val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
            incompletePropertiesPage.firstSummaryCard.continueLink.clickAndWait()
            val taskListPage = assertPageIs(page, TaskListPagePropertyRegistration::class)

            taskListPage.backLink.clickAndWait()
            assertPageIs(page, LandlordIncompletePropertiesPage::class)
        }

        @Test
        fun `Clicking on a summary card Delete link redirects to the task list page`(page: Page) {
            val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
            incompletePropertiesPage.firstSummaryCard.deleteLink.clickAndWait()
            assertPageIs(page, DeleteIncompletePropertyRegistrationAreYouSurePage::class, mapOf("contextId" to "1"))
        }
    }

    @Nested
    inner class LandlordsWithNoIncompleteProperties : NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-properties.sql") {
        @Test
        fun `the page loads with heading and page text`() {
            val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
            assertThat(incompletePropertiesPage.heading).containsText("Your incomplete properties")
            assertThat(incompletePropertiesPage.subHeading).containsText("You have no incomplete properties.")
            assertThat(incompletePropertiesPage.text).containsText("You can either view registered properties or register a new property.")
        }

        @Test
        fun `the view registered properties link goes to the property records tab on the landlord details page`(page: Page) {
            val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
            incompletePropertiesPage.viewRegisteredPropertiesLink.clickAndWait()
            val detailsPage = assertPageIs(page, LandlordDetailsPage::class)
            assertEquals("registered-properties", detailsPage.tabs.activeTabPanelId)
        }

        @Test
        fun `the register a new property link goes to the property registration start page`(page: Page) {
            val incompletePropertiesPage = navigator.goToLandlordIncompleteProperties()
            incompletePropertiesPage.registerANewPropertyLink.clickAndWait()
            assertPageIs(page, RegisterPropertyStartPage::class)
        }
    }
}
