package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SecondaryButton
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table

open class CheckElectricalCertUploadsFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    override val expectedTitleHeading: String
        get() = table.rows.count().let { "You’ve uploaded $it electrical safety certificate file${if (it == 1) "" else "s"}" }

    val backLink = BackLink.default(page)
    val table = CheckUploadsTable(page)
    val form = CheckUploadsForm(page)

    class CheckUploadsForm(
        page: Page,
    ) : PostForm(page) {
        val addAnotherButton = SecondaryButton(locator)
    }

    class CheckUploadsTable(
        page: Page,
    ) : Table(page) {
        val actionsHeader = headerRow.getCell(2)
        val actionsHeaderVisuallyHiddenText = headerRow.getVisuallyHiddenText(2)
    }
}
