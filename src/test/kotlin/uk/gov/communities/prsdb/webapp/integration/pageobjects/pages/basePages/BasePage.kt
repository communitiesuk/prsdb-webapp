package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages

import com.deque.html.axecore.playwright.AxeBuilder
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.TextInput
import kotlin.reflect.KClass
import kotlin.test.assertContains
import kotlin.test.assertEquals

abstract class BasePage(
    val page: Page,
    private val urlSegment: String? = null,
) {
    companion object {
        fun <T : BasePage> createValid(
            page: Page,
            targetClass: KClass<T>,
        ): T {
            page.waitForLoadState()
            val pageInstance = targetClass.constructors.first().call(page)
            pageInstance.validate()
            val axeResults =
                AxeBuilder(page)
                    .withTags(listOf("wcag2a", "wcag2aa", "wcag21a", "wcag21aa"))
                    .analyze()
            assertEquals(
                emptyList(),
                axeResults.violations,
                "There were Axe violations after creating and validating a ${targetClass.simpleName}",
            )
            return pageInstance
        }

        fun <T : BasePage> assertIsPage(
            page: Page,
            targetClass: KClass<T>,
        ): T = createValid(page, targetClass)
    }

    protected val header: Locator = page.locator("main header h1")

    open fun validate() {
        if (urlSegment != null) {
            assertContains(page.url(), urlSegment)
        }
    }

    protected fun inputFormGroup(fieldName: String) = TextInput(page.locator(".govuk-form-group:has(>input[name=\"$fieldName\"])"))
}
