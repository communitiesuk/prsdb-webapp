package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
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

        // Submit changes TODO PRSD-355 add proper submit button and declaration page
        propertyDetailsUpdatePage.submitButton.clickAndWait()
        propertyDetailsUpdatePage = assertPageIs(page, PropertyDetailsUpdatePage::class)

        // Check changes have occurred
        assertThat(propertyDetailsUpdatePage.propertyDetailsSummaryList.ownershipTypeRow.value).containsText("Leasehold")
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
}
