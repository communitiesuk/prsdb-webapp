package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.HouseholdsOccupancyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.PeopleNumberOfHouseholdsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.PeopleOccupancyFormPagePropertyDetailsUpdate
import kotlin.test.assertEquals

class PropertyDetailsUpdateSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Test
    fun `Skipped occupancy update sub-journey steps can be accessed via CYA page links`(page: Page) {
        // Household occupancy update page
        val checkHouseholdsAnswersPage = navigator.goToPropertyDetailsUpdateCheckHouseholdAnswersPage(propertyOwnershipId)
        checkHouseholdsAnswersPage.form.summaryList.occupancyRow
            .clickActionLinkAndWait()
        val householdOccupancyUpdatePage = assertPageIs(page, HouseholdsOccupancyFormPagePropertyDetailsUpdate::class, urlArguments)
        assertEquals("true", householdOccupancyUpdatePage.form.occupancyRadios.selectedValue)

        // People occupancy update page
        var checkPeopleAnswersPage = navigator.goToPropertyDetailsUpdateCheckPeopleAnswersPage(propertyOwnershipId)
        checkPeopleAnswersPage.form.summaryList.occupancyRow
            .clickActionLinkAndWait()
        val peopleOccupancyUpdatePage = assertPageIs(page, PeopleOccupancyFormPagePropertyDetailsUpdate::class, urlArguments)
        assertEquals("true", peopleOccupancyUpdatePage.form.occupancyRadios.selectedValue)

        // People number of households update page
        checkPeopleAnswersPage = navigator.goToPropertyDetailsUpdateCheckPeopleAnswersPage(propertyOwnershipId)
        checkPeopleAnswersPage.form.summaryList.numberOfHouseholdsRow
            .clickActionLinkAndWait()
        val peopleNumberOfHouseholdsUpdatePage =
            assertPageIs(page, PeopleNumberOfHouseholdsFormPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(peopleNumberOfHouseholdsUpdatePage.form.householdsInput).hasValue("1")
    }
}
