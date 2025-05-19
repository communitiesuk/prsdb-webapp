package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.JourneyForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class AreYouSureFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = AreYouSureForm(page)

    val backLink = BackLink.default(page)

    fun submitWantsToProceed() {
        form.areYouSureRadios.selectValue("true")
        form.submit()
    }

    fun submitDoesNotWantToProceed() {
        form.areYouSureRadios.selectValue("false")
        form.submit()
    }

    class AreYouSureForm(
        page: Page,
    ) : JourneyForm(page) {
        val areYouSureRadios = Radios(locator)
    }
}
