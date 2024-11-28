package uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.Form

abstract class FormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = Form(page)
}
