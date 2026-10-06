package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.deque.html.axecore.playwright.AxeBuilder
import com.microsoft.playwright.Page
import kotlin.reflect.KClass
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

abstract class BasePage(
    val page: Page,
    private val urlSegment: String? = null,
) {
    protected open val expectedTitleHeading: String
        get() {
            val h1 = page.locator("h1").first()
            assertEquals(1, h1.count(), "Page has no h1 to build its title from")
            return h1.textContent().replace(Regex("\\s+"), " ").trim()
        }

    companion object {
        // TODO PDJB-1364: Enable once every page's title is built from its h1
        const val STRICT_TITLE_CHECK = false

        private val TITLE_FORMAT =
            Regex("^(Error: )?(.+) - (Register your rental property|Check a rental property or landlord) - GOV\\.UK$")

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

    protected open fun validateTitle() {
        val title = page.title()
        val match =
            assertNotNull(
                TITLE_FORMAT.matchEntire(title),
                "Page title \"$title\" does not match the format \"[Error: ]<heading> - <service name> - GOV.UK\"",
            )
        val titleHeading = match.groupValues[2]
        assertNotEquals("null", titleHeading, "Page title \"$title\" is missing its heading")

        if (STRICT_TITLE_CHECK) {
            assertEquals(
                expectedTitleHeading,
                titleHeading,
                "Page title should match the h1 (override expectedTitleHeading if it deliberately differs)",
            )
        }
    }

    private fun getAxeViolations() =
        AxeBuilder(page)
            .withTags(listOf("wcag2a", "wcag2aa", "wcag21a", "wcag21aa"))
            .exclude(listOf("input[type='radio', aria-expanded]"))
            .analyze()
            .violations
}
