package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

abstract class CheckOccupancyAnswersFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = Form(page)

    fun confirm() = form.submit()

    val summaryList = CheckOccupancyAnswersPropertyDetailsSummaryList(page)

    class CheckOccupancyAnswersPropertyDetailsSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val occupancyRow = getRow("Occupied by tenants")
        val numberOfHouseholdsRow = getRow("Number of households")
        val numberOfPeopleRow = getRow("Number of tenants")
        val numberOfBedroomsRow = getRow("Number of bedrooms")
        val rentIncludesBillsRow = getRow("Rent includes bills")
        val billsIncludedRow = getRow("Which bills are included")
        val furnishedStatusRow = getRow("Furniture provided")
        val rentFrequencyRow = getRow("When rent is paid")
        val rentAmountRow = getRow("Rent amount")
    }
}
