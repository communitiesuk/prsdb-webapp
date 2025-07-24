package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class EpcExpiryCheckBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = EpcExpiryCheckForm(page)

    fun submitTenancyStartedBeforeExpiry() {
        form.tenancyStartedBeforeExpiryRadios.selectValue("true")
        form.submit()
    }

    fun submitTenancyStartedAfterExpiry() {
        form.tenancyStartedBeforeExpiryRadios.selectValue("false")
        form.submit()
    }

    class EpcExpiryCheckForm(
        page: Page,
    ) : PostForm(page) {
        val tenancyStartedBeforeExpiryRadios = Radios(locator)
    }
}
