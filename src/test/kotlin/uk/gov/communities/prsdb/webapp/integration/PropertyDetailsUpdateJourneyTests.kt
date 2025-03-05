package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OwnershipTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.PropertyDetailsUpdatePage

@Sql("/data-local.sql")
class PropertyDetailsUpdateJourneyTests : IntegrationTest() {
    @Test
    fun `A property's details can all be updated in one session`(page: Page) {
        // Update details page
        var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage()
        assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

        val newOwnershipType = OwnershipType.LEASEHOLD
        propertyDetailsUpdatePage = updateOwnershipTypeAndReturn(propertyDetailsUpdatePage, newOwnershipType)

        val newNumberOfPeople = 4
        propertyDetailsUpdatePage = updateNumberOfPeopleAndReturn(propertyDetailsUpdatePage, newNumberOfPeople)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.ownershipTypeRow.value).containsText("Leasehold")
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.numberOfPeopleRow.value).containsText(
            newNumberOfPeople.toString(),
        )
    }

    @Test
    fun `A property can have just their ownership type updated`(page: Page) {
        // Update details page
        var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage()
        assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

        val newOwnershipType = OwnershipType.LEASEHOLD
        propertyDetailsUpdatePage = updateOwnershipTypeAndReturn(propertyDetailsUpdatePage, newOwnershipType)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.ownershipTypeRow.value).containsText("Leasehold")
    }

    @Test
    fun `A property can have just their number of people updated`(page: Page) {
        // Update details page
        var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage()
        assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

        val newNumberOfPeople = 3
        propertyDetailsUpdatePage = updateNumberOfPeopleAndReturn(propertyDetailsUpdatePage, newNumberOfPeople)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.numberOfPeopleRow.value).containsText(
            newNumberOfPeople.toString(),
        )
    }

    private fun updateOwnershipTypeAndReturn(
        detailsPage: PropertyDetailsUpdatePage,
        newOwnershipType: OwnershipType,
    ): PropertyDetailsUpdatePage {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.ownershipTypeRow.clickActionLinkAndWait()

        val updateOwnershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyDetailsUpdate::class)
        updateOwnershipTypePage.submitOwnershipType(newOwnershipType)

        return assertPageIs(page, PropertyDetailsUpdatePage::class)
    }

    private fun updateNumberOfPeopleAndReturn(
        detailsPage: PropertyDetailsUpdatePage,
        newNumberOfPeople: Int,
    ): PropertyDetailsUpdatePage {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.numberOfPeopleRow.clickActionLinkAndWait()

        val updateNumberOfPeoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class)
        updateNumberOfPeoplePage.submitNumOfPeople(newNumberOfPeople)

        return assertPageIs(page, PropertyDetailsUpdatePage::class)
    }
}
