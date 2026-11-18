package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

abstract class CheckPropertyManagerFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = Form(page)
    val backLink = BackLink.default(page)
    val propertyManagerCard = PropertyManagerSummaryCard(page)

    fun clickChangeAndWait() = propertyManagerCard.getAction("Change").link.clickAndWait()

    fun clickRemoveAndWait() = propertyManagerCard.getAction("Remove").link.clickAndWait()

    class PropertyManagerSummaryCard(
        page: Page,
    ) : SummaryCard(page) {
        override val summaryList = PropertyManagerSummaryList(locator)

        class PropertyManagerSummaryList(
            parentLocator: Locator,
        ) : SummaryList(parentLocator) {
            val emailAddressRow = getRow("Email address")
        }
    }
}
