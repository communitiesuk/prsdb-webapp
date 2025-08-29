package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.INVALID_PASSCODE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InvalidPasscodePage(
    page: Page,
) : BasePage(page, INVALID_PASSCODE_PATH_SEGMENT) {
    val enterPasscodeButton = Button.byText(page, "Enter a passcode")
}
