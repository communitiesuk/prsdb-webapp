package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class OrgNameFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = OrgNameForm(page)

    fun submitName(name: String) {
        form.orgNameInput.fill(name)
        form.submit()
    }

    class OrgNameForm(
        page: Page,
    ) : PostForm(page) {
        val orgNameInput = TextInput.textByFieldName(locator, "orgName")
    }
}
