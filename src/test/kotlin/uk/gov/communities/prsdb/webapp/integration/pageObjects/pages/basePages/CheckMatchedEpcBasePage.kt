package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class CheckMatchedEpcBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = CheckMatchedEpcForm(page)

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
}
