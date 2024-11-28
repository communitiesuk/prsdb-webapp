package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.deque.html.axecore.playwright.AxeBuilder
import com.microsoft.playwright.Page
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
            expectedPageClass: KClass<T>,
        ): T {
            page.waitForLoadState()
            val pageInstance = expectedPageClass.constructors.first().call(page)
            pageInstance.validate()
            assertEquals(
                emptyList(),
                pageInstance.getAxeViolations(),
                "There were Axe violations after creating and validating a ${expectedPageClass.simpleName}",
            )
            return pageInstance
        }

        fun <T : BasePage> assertPageIs(
            page: Page,
            expectedPageClass: KClass<T>,
        ) = createValidPage(page, expectedPageClass)
    }

    private fun validate() = assertContains(page.url(), urlSegment)

    private fun getAxeViolations() =
        AxeBuilder(page)
            .withTags(listOf("wcag2a", "wcag2aa", "wcag21a", "wcag21aa"))
            .exclude(listOf("input[type='radio', aria-expanded]"))
            .analyze()
            .violations
}
