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
        val occupancyRow = getRow("Is your property occupied by tenants?")
        val numberOfHouseholdsRow = getRow("Households in your property")
        val numberOfPeopleRow = getRow("How many people live in your property?")
    }
}
