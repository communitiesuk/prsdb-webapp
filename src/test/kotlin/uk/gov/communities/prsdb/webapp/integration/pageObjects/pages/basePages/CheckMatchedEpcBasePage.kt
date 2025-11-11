package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

abstract class CheckMatchedEpcBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = CheckMatchedEpcForm(page)

    var heading = Heading(page.locator("h1"))

    val summaryList = CheckMatchedEpcSummaryList(page)

    fun submitMatchedEpcDetailsCorrect() {
        form.matchedEpcDetailsCorrectRadios.selectValue("true")
        form.submit()
    }

    fun submitMatchedEpcDetailsIncorrect() {
        form.matchedEpcDetailsCorrectRadios.selectValue("false")
        form.submit()
    }

    class CheckMatchedEpcForm(
        page: Page,
    ) : PostForm(page) {
        val matchedEpcDetailsCorrectRadios = Radios(locator)
    }

    class CheckMatchedEpcSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val addressRow = getRow("Address")
        val energyRatingRow = getRow("Energy rating")
        val expiryDateRow = getRow("Expiry date")
    }
}
