package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.deque.html.axecore.playwright.AxeBuilder
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import kotlin.reflect.KClass
import kotlin.test.assertContains
import kotlin.test.assertEquals

abstract class BasePage(
    val page: Page,
    private val urlSegment: String,
) {
    companion object {
        fun <T : BasePage> createValidPage(
            page: Page,
            targetClass: KClass<T>,
        ): T {
            page.waitForLoadState()
            val pageInstance = targetClass.constructors.first().call(page)
            pageInstance.validate()
            val axeResults =
                AxeBuilder(page)
                    .withTags(listOf("wcag2a", "wcag2aa", "wcag21a", "wcag21aa"))
                    .exclude(listOf("input[type='radio', aria-expanded]"))
                    .analyze()
            assertEquals(
                emptyList(),
                axeResults.violations,
                "There were Axe violations after creating and validating a ${targetClass.simpleName}",
            )
            return pageInstance
        }
    }

    private fun validate() = assertContains(page.url(), urlSegment)

    protected inline fun <reified T : BasePage> clickElementAndAssertNextPage(locator: Locator): T {
        locator.click()
        return createValidPage(page, T::class)
    }

    protected fun submitInvalidForm(form: Form) {
        form.getSubmitButton().click()
        page.waitForLoadState()
    }
}