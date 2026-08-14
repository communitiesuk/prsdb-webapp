package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

abstract class OrgGovBodyMemberListFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("h1"))
    val form = PostForm(page)
    val summaryList = GovBodyMemberSummaryList(page)
    val addAnotherButton: Locator = page.locator("a.govuk-button--secondary")

    fun getChangeActionLink(rowIndex: Int) = summaryList.getRowByIndex(rowIndex).actions.getActionLink("Change")

    fun getRemoveActionLink(rowIndex: Int) = summaryList.getRowByIndex(rowIndex).actions.getActionLink("Remove")

    fun submit() {
        form.submit()
    }

    class GovBodyMemberSummaryList(
        page: Page,
    ) : SummaryList(page) {
        fun getRowByIndex(index: Int) = getRow(index)
    }
}
