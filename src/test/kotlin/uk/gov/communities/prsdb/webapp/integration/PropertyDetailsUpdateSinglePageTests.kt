package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HouseholdsOccupancyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfHouseholdsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.PeopleNumberOfHouseholdsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.PeopleOccupancyFormPagePropertyDetailsUpdate
import kotlin.test.assertEquals

class PropertyDetailsUpdateSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Test
    fun `Skipped occupancy update sub-journey steps can be accessed via CYA page links, and back links when changing answers`(page: Page) {
        // Household occupancy update page
        val checkHouseholdsAnswersPage = navigator.skipToPropertyDetailsUpdateCheckHouseholdAnswersPage(propertyOwnershipId)
        checkHouseholdsAnswersPage.form.summaryList.occupancyRow
            .clickActionLinkAndWait()
        val householdOccupancyUpdatePage = assertPageIs(page, HouseholdsOccupancyFormPagePropertyDetailsUpdate::class, urlArguments)
        assertEquals("true", householdOccupancyUpdatePage.form.occupancyRadios.selectedValue)

        householdOccupancyUpdatePage.form.submit()
        val numberOfHouseholdsPage = assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, urlArguments)
        numberOfHouseholdsPage.backLink.clickAndWait()
        assertPageIs(page, HouseholdsOccupancyFormPagePropertyDetailsUpdate::class, urlArguments)

        // People occupancy update page
        var checkPeopleAnswersPage = navigator.skipToPropertyDetailsUpdateCheckPeopleAnswersPage(propertyOwnershipId)
        checkPeopleAnswersPage.form.summaryList.occupancyRow
            .clickActionLinkAndWait()
        val peopleOccupancyUpdatePage = assertPageIs(page, PeopleOccupancyFormPagePropertyDetailsUpdate::class, urlArguments)
        assertEquals("true", peopleOccupancyUpdatePage.form.occupancyRadios.selectedValue)

        peopleOccupancyUpdatePage.form.submit()
        val peopleNumberOfHouseholdsPage = assertPageIs(page, PeopleNumberOfHouseholdsFormPagePropertyDetailsUpdate::class, urlArguments)
        peopleNumberOfHouseholdsPage.backLink.clickAndWait()
        assertPageIs(page, PeopleOccupancyFormPagePropertyDetailsUpdate::class, urlArguments)

        // People number of households update page
        checkPeopleAnswersPage = navigator.skipToPropertyDetailsUpdateCheckPeopleAnswersPage(propertyOwnershipId)
        checkPeopleAnswersPage.form.summaryList.numberOfHouseholdsRow
            .clickActionLinkAndWait()
        val peopleNumberOfHouseholdsUpdatePage =
            assertPageIs(page, PeopleNumberOfHouseholdsFormPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(peopleNumberOfHouseholdsUpdatePage.form.householdsInput).hasValue("1")

        peopleNumberOfHouseholdsUpdatePage.form.submit()
        val numberOfPeoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class, urlArguments)
        numberOfPeoplePage.backLink.clickAndWait()
        assertPageIs(page, PeopleNumberOfHouseholdsFormPagePropertyDetailsUpdate::class, urlArguments)
    }

    @Test
    fun `Submitting a CYA page with stale data redirects to an error page`(browserContext: BrowserContext) {
        // Create two pages
        val (page1, navigator1) = createPageAndNavigator(browserContext)
        val (_, navigator2) = createPageAndNavigator(browserContext)

        // Navigate to the occupancy check answers page on page1 with an occupied property
        val checkOccupancyAnswersPage = navigator1.skipToPropertyDetailsUpdateCheckOccupancyToOccupiedAnswersPage(propertyOwnershipId)

        // Update occupancy to vacant on page2
        navigator2.skipToPropertyDetailsUpdateCheckOccupancyToVacantAnswersPage(propertyOwnershipId)

        // Submit the occupancy check answers page on page1
        checkOccupancyAnswersPage.form.submit()

        // Assert that the page1 is redirected to an error page
        val errorPage = assertPageIs(page1, ErrorPage::class)
        assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
    }
}
