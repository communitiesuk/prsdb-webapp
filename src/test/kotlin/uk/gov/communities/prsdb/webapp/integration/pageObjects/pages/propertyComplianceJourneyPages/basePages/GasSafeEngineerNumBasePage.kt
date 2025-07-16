package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

open class GasSafeEngineerNumBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = GasSafeEngineerNumForm(page)

    fun submitEngineerNum(engineerNum: String) {
        form.engineerNumberInput.fill(engineerNum)
        form.submit()
    }

    class GasSafeEngineerNumForm(
        page: Page,
    ) : PostForm(page) {
        val engineerNumberInput = TextInput.textByFieldName(locator, "engineerNumber")
    }
}
