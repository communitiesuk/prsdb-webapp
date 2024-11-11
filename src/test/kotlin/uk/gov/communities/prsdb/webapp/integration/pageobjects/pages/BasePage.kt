package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.TextInput
import kotlin.reflect.KClass

abstract class BasePage(
    protected val page: Page,
) {
    companion object {
        fun <T : BasePage> createValid(
            page: Page,
            targetClass: KClass<T>,
        ): T {
            page.waitForLoadState()
            val pageInstance = targetClass.constructors.first().call(page)
            pageInstance.validate()
            return pageInstance
        }
    }

    protected val header = page.locator("main header h1")

    protected val fieldSetHeading = page.locator(".govuk-fieldset__heading")

    abstract fun validate()

    protected fun inputFormGroup(fieldName: String) = TextInput(page.locator(".govuk-form-group:has(input[name=\"$fieldName\"])"))

    protected fun fieldsetInput(fieldName: String) = TextInput(page.locator(".govuk-fieldset:has(input[name=\"$fieldName\"])"))
}
