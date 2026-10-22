package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.deque.html.axecore.playwright.AxeBuilder
import com.microsoft.playwright.Page
import kotlin.reflect.KClass
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class BasePage(
    val page: Page,
    private val urlSegment: String? = null,
) {
    protected open val expectedTitleHeading: String? = null

    protected open val expectedTitleHeadingPattern: Regex? = null

    private val h1Text: String
        get() {
            val h1 = page.locator("h1").first()
            assertEquals(1, h1.count(), "Page has no h1 to build its title from")
            return h1.textContent().replace(Regex("\\s+"), " ").trim()
        }

    companion object {
        private const val SERVICE_NAMES = "(Register your rental property|Check a rental property or landlord)"

        fun <T : BasePage> createValidPage(
            page: Page,
            expectedPageClass: KClass<T>,
            urlArguments: Map<String, String>? = null,
        ): T {
            page.waitForLoadState()
            val pageInstance =
                if (urlArguments != null) {
                    expectedPageClass.constructors.first().call(page, urlArguments)
                } else {
                    expectedPageClass.constructors.first().call(page)
                }
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
            urlArguments: Map<String, String>? = null,
        ) = createValidPage(page, expectedPageClass, urlArguments)
    }

    private fun validate() {
        if (urlSegment != null) assertContains(page.url(), urlSegment)
        validateTitle()
    }

    private fun validateTitle() {
        val title = page.title()
        val expectedHeading = expectedTitleHeadingPattern?.pattern ?: Regex.escape(expectedTitleHeading ?: h1Text)
        assertTrue(
            Regex("^(Error: )?$expectedHeading - $SERVICE_NAMES - GOV\\.UK$").matches(title),
            "Page title \"$title\" should match \"[Error: ]$expectedHeading - <service name> - GOV.UK\" " +
                "(override expectedTitleHeading if the title deliberately differs from the h1)",
        )
    }

    private fun getAxeViolations() =
        AxeBuilder(page)
            .withTags(listOf("wcag2a", "wcag2aa", "wcag21a", "wcag21aa"))
            .exclude(listOf("input[type='radio', aria-expanded]"))
            .analyze()
            .violations
}
