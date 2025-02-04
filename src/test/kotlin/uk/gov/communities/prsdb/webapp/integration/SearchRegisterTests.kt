package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage.Companion.ADDRESS_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage.Companion.CONTACT_INFO_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage.Companion.LANDLORD_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage.Companion.LISTED_PROPERTY_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertContains
import kotlin.test.assertTrue

@Sql("/data-landlord-search.sql")
class SearchRegisterTests : IntegrationTest() {
    @Test
    fun `results table does not show before search has been requested`() {
        val searchLandlordRegisterPage = navigator.goToSearchLandlordRegister()

        val exception = assertThrows<AssertionFailedError> { searchLandlordRegisterPage.getResultTable() }
        assertContains(exception.message!!, "Expected 1 instance of Locator@.govuk-table, found 0")
    }

    @Test
    fun `results table does not show when blank search term requested`() {
        val searchLandlordRegisterPage = navigator.goToSearchLandlordRegister()
        searchLandlordRegisterPage.searchBar.search("")

        val exception = assertThrows<AssertionFailedError> { searchLandlordRegisterPage.getResultTable() }
        assertContains(exception.message!!, "Expected 1 instance of Locator@.govuk-table, found 0")
    }

    @Test
    fun `results table shows after search has been requested`() {
        val searchLandlordRegisterPage = navigator.goToSearchLandlordRegister()
        searchLandlordRegisterPage.searchBar.search("L-CKSQ-3SX9")
        val resultTable = searchLandlordRegisterPage.getResultTable()

        assertThat(resultTable.getHeaderCell(LANDLORD_COL_INDEX)).containsText("Landlord")
        assertThat(resultTable.getCell(0, LANDLORD_COL_INDEX)).containsText("Alexander Smith\nL-CKSQ-3SX9")

        assertThat(resultTable.getHeaderCell(ADDRESS_COL_INDEX)).containsText("Contact address")
        assertThat(resultTable.getCell(0, ADDRESS_COL_INDEX)).containsText("1 Fictional Road")

        assertThat(resultTable.getHeaderCell(CONTACT_INFO_COL_INDEX)).containsText("Contact information")
        assertThat(resultTable.getCell(0, CONTACT_INFO_COL_INDEX)).containsText("7111111111\nalex.surname@example.com")

        assertThat(resultTable.getHeaderCell(LISTED_PROPERTY_COL_INDEX)).containsText("Listed properties")
        assertThat(resultTable.getCell(0, LISTED_PROPERTY_COL_INDEX)).containsText("3")

        val exception = assertThrows<AssertionFailedError> { searchLandlordRegisterPage.getErrorMessage() }
        assertContains(exception.message!!, "Expected 1 instance of Locator@#no-results >> nth=0, found 0")
    }

    @Test
    fun `fuzzy search functionality produces table of matching results`() {
        val searchLandlordRegisterPage = navigator.goToSearchLandlordRegister()
        searchLandlordRegisterPage.searchBar.search("Alex")
        val resultTable = searchLandlordRegisterPage.getResultTable()

        assertThat(resultTable.getCell(0, LANDLORD_COL_INDEX)).containsText("Alexander Smith")
        assertThat(resultTable.getCell(1, LANDLORD_COL_INDEX)).containsText("Alexandra Davies")
        assertThat(resultTable.getCell(2, LANDLORD_COL_INDEX)).containsText("Evan Alexandrescu")
    }

    @Test
    fun `landlord links goes to landlord details page`(page: Page) {
        val searchLandlordRegisterPage = navigator.goToSearchLandlordRegister()
        searchLandlordRegisterPage.searchBar.search("L-CKSQ-3SX9")
        searchLandlordRegisterPage.getLandlordLink(rowIndex = 0).click()
        assertContains(page.url(), "/landlord-details/1")
    }

    @Test
    fun `error shows if search has no results`() {
        val searchLandlordRegisterPage = navigator.goToSearchLandlordRegister()
        searchLandlordRegisterPage.searchBar.search("non-matching searchTerm")

        assertContains(searchLandlordRegisterPage.getErrorMessageText(), "No landlord record found")
    }

    @Test
    fun `property search link shows if search has no results`(page: Page) {
        val searchLandlordRegisterPage = navigator.goToSearchLandlordRegister()
        searchLandlordRegisterPage.searchBar.search("non-matching searchTerm")
        searchLandlordRegisterPage.getPropertySearchLink().click()

        // TODO PRSD-659: Replace with landlord details page assertion
        assertPageIs(page, ErrorPage::class)
        assertContains(page.url(), "/search/property")
    }

    @Test
    fun `pagination component does not show if there is only one page of results`(page: Page) {
        val searchLandlordRegisterPage = navigator.goToSearchLandlordRegister()
        searchLandlordRegisterPage.searchBar.search("Alex")

        val exception = assertThrows<AssertionFailedError> { searchLandlordRegisterPage.getPaginationComponent() }
        assertContains(exception.message!!, "Expected 1 instance of Locator@.govuk-pagination >> nth=0, found 0")
    }

    @Test
    fun `pagination links lead to the intended pages`(page: Page) {
        val searchLandlordRegisterPage = navigator.goToSearchLandlordRegister()
        searchLandlordRegisterPage.searchBar.search("PRSDB")

        searchLandlordRegisterPage.getPaginationComponent().getNextLink().click()
        assertContains(page.url(), "page=2")
        val nextPage = assertPageIs(page, SearchLandlordRegisterPage::class)

        nextPage.getPaginationComponent().getPreviousLink().click()
        assertContains(page.url(), "page=1")
        val previousPage = assertPageIs(page, SearchLandlordRegisterPage::class)

        previousPage.getPaginationComponent().getPageNumberLink(2).click()
        assertContains(page.url(), "page=2")
        assertPageIs(page, SearchLandlordRegisterPage::class)
    }

    @Test
    fun `filter panel can be toggled and used to refine search results`(page: Page) {
        val searchLandlordRegisterPage = navigator.goToSearchLandlordRegister()
        searchLandlordRegisterPage.searchBar.search("Alex")

        val filter = searchLandlordRegisterPage.getFilterPanel()

        // Toggle filter
        filter.clickCloseFilterPanel()
        val exception = assertThrows<AssertionFailedError> { filter.getPanel() }
        assertContains(
            exception.message!!,
            "Expected 1 instance of Locator@.moj-filter-layout >> .moj-filter >> nth=0, found 0",
        )

        filter.clickShowFilterPanel()
        assertTrue(filter.getPanel().isVisible)

        // Apply LA filter
        val laFilter = filter.getFilterCheckboxes("Show landlords operating in my authority")
        laFilter.checkCheckbox("true")
        filter.clickApplyFiltersButton()

        val resultTable = searchLandlordRegisterPage.getResultTable()
        assertTrue(resultTable.countRows() == 1)

        // Remove LA filter
        filter.clickRemoveFilterTag("Landlords in my authority")
        assertTrue(resultTable.countRows() > 1)

        // Clear all filters
        laFilter.checkCheckbox("true")
        filter.clickApplyFiltersButton()

        filter.clickClearFiltersLink()
        assertTrue(resultTable.countRows() > 1)
    }
}
