package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.INVALID_PASSCODE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InvalidPasscodePage(
    page: Page,
) : BasePage(page, INVALID_PASSCODE_PATH_SEGMENT) {
    val goBackLink = Link.byText(page, "Go back")
}
