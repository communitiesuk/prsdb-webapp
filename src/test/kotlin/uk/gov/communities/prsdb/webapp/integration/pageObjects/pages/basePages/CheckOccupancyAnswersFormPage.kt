package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

abstract class CheckOccupancyAnswersFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = CheckOccupancyAnswersPropertyDetailsUpdateForm(page)

    fun confirm() = form.submit()

    class CheckOccupancyAnswersPropertyDetailsUpdateForm(
        page: Page,
    ) : Form(page) {
        val summaryList = CheckLicensingAnswersPropertyDetailsSummaryList(locator)
    }

    class CheckLicensingAnswersPropertyDetailsSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val occupancyRow = getRow("Is your property occupied by tenants?")
        val numberOfHouseholdsRow = getRow("How many households live in your property?")
        val numberOfPeopleRow = getRow("How many people live in your property?")
    }
}
