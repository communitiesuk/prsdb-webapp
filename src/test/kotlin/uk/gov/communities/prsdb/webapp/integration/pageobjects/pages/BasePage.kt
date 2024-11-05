package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.TextInput

abstract class BasePage(
    protected val page: Page,
) {
    companion object {
        inline fun <reified T : BasePage> createValid(page: Page): T {
            val pageInstance = T::class.constructors.first().call(page)
            pageInstance.validate()
            return pageInstance
        }
    }

    protected val header = page.locator("main header h1")

    abstract fun validate()

    protected fun inputFormGroup(fieldName: String) = TextInput(page.locator(".govuk-form-group:has(input[name=\"$fieldName\"])"))
}
