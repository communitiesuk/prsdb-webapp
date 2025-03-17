package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfHouseholdsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.NumberOfPeopleFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OwnershipTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.PropertyDetailsUpdatePage

@Sql("/data-local.sql")
class PropertyDetailsUpdateJourneyTests : IntegrationTest() {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Test
    fun `A property's details can all be updated in one session`(page: Page) {
        // Update details page
        var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(propertyOwnershipId)
        assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

        val newOwnershipType = OwnershipType.LEASEHOLD
        propertyDetailsUpdatePage = updateOwnershipTypeAndReturn(propertyDetailsUpdatePage, newOwnershipType)

        val newNumberOfHouseholds = 2
        propertyDetailsUpdatePage = updateNumberOfHouseholdsAndReturn(propertyDetailsUpdatePage, newNumberOfHouseholds)

        val newNumberOfPeople = 4
        propertyDetailsUpdatePage = updateNumberOfPeopleAndReturn(propertyDetailsUpdatePage, newNumberOfPeople)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.ownershipTypeRow.value).containsText("Leasehold")
        assertThat(
            propertyDetailsUpdatePage.propertyDetailsSummaryList.numberOfHouseholdsRow.value,
        ).containsText(newNumberOfHouseholds.toString())
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.numberOfPeopleRow.value).containsText(
            newNumberOfPeople.toString(),
        )
    }

    @Test
    fun `A property can have just their ownership type updated`(page: Page) {
        // Update details page
        var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(propertyOwnershipId)
        assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

        val newOwnershipType = OwnershipType.LEASEHOLD
        propertyDetailsUpdatePage = updateOwnershipTypeAndReturn(propertyDetailsUpdatePage, newOwnershipType)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.ownershipTypeRow.value).containsText("Leasehold")
    }

    @Test
    fun `A property can have just their number of households updated`(page: Page) {
        // Update details page
        var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(propertyOwnershipId)
        assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

        val newNumberOfHouseholds = 2
        propertyDetailsUpdatePage = updateNumberOfHouseholdsAndReturn(propertyDetailsUpdatePage, newNumberOfHouseholds)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.numberOfHouseholdsRow.value).containsText(
            newNumberOfHouseholds.toString(),
        )
    }

    @Test
    fun `A property can have just their number of people updated`(page: Page) {
        // Update details page
        var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(propertyOwnershipId)
        assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

        val newNumberOfPeople = 3
        propertyDetailsUpdatePage = updateNumberOfPeopleAndReturn(propertyDetailsUpdatePage, newNumberOfPeople)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.numberOfPeopleRow.value).containsText(
            newNumberOfPeople.toString(),
        )
    }

    @Test
    fun `When number of households is updated to less than number of people the user is redirected to number of people page`(page: Page) {
        // Update details page
        var propertyDetailsUpdatePage = navigator.goToPropertyDetailsUpdatePage(propertyOwnershipId)
        assertThat(propertyDetailsUpdatePage.heading).containsText("1, Example Road, EG")

        val newNumberOfHouseholds = 3
        val numberOfPeopleUpdatePage = updateNumberOfHouseholdsAndReturnNumberOfPeople(propertyDetailsUpdatePage, newNumberOfHouseholds)

        val newNumberOfPeople = 4
        propertyDetailsUpdatePage = updateNumberOfPeopleAndReturn(page, numberOfPeopleUpdatePage, newNumberOfPeople)

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.numberOfHouseholdsRow.value).containsText(
            newNumberOfHouseholds.toString(),
        )
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

        val updateOwnershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyDetailsUpdate::class, urlArguments)
        updateOwnershipTypePage.submitOwnershipType(newOwnershipType)

        return assertPageIsPropertyDetailsUpdatePage(page)
    }

    private fun updateNumberOfHouseholdsAndReturn(
        detailsPage: PropertyDetailsUpdatePage,
        newNumberOfHouseholds: Int,
    ): PropertyDetailsUpdatePage {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.clickActionLinkAndWait()

        val updateNumberOfHouseholdsPage = assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, urlArguments)
        updateNumberOfHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)

        return assertPageIsPropertyDetailsUpdatePage(page)
    }

    private fun updateNumberOfHouseholdsAndReturnNumberOfPeople(
        detailsPage: PropertyDetailsUpdatePage,
        newNumberOfHouseholds: Int,
    ): NumberOfPeopleFormPagePropertyDetailsUpdate {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.numberOfHouseholdsRow.clickActionLinkAndWait()

        val updateNumberOfHouseholdsPage = assertPageIs(page, NumberOfHouseholdsFormPagePropertyDetailsUpdate::class, urlArguments)
        updateNumberOfHouseholdsPage.submitNumberOfHouseholds(newNumberOfHouseholds)

        return assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class, urlArguments)
    }

    private fun updateNumberOfPeopleAndReturn(
        detailsPage: PropertyDetailsUpdatePage,
        newNumberOfPeople: Int,
    ): PropertyDetailsUpdatePage {
        val page = detailsPage.page
        detailsPage.propertyDetailsSummaryList.numberOfPeopleRow.clickActionLinkAndWait()

        val updateNumberOfPeoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyDetailsUpdate::class, urlArguments)

        return updateNumberOfPeopleAndReturn(page, updateNumberOfPeoplePage, newNumberOfPeople)
    }

    private fun updateNumberOfPeopleAndReturn(
        page: Page,
        numberOfPeopleUpdatePage: NumberOfPeopleFormPagePropertyDetailsUpdate,
        newNumberOfPeople: Int,
    ): PropertyDetailsUpdatePage {
        numberOfPeopleUpdatePage.submitNumOfPeople(newNumberOfPeople)

        return assertPageIsPropertyDetailsUpdatePage(page)
    }

    private fun assertPageIsPropertyDetailsUpdatePage(page: Page) = assertPageIs(page, PropertyDetailsUpdatePage::class, urlArguments)
}
