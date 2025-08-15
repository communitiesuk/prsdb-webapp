package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.PASSCODE_ALREADY_USED_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PasscodeAlreadyUsedPage(
    page: Page,
) : BasePage(page, PASSCODE_ALREADY_USED_PATH_SEGMENT) {
    val heading = Heading(page.locator("main h1"))
    val bodyText = page.locator("p.govuk-body")
    val tryAnotherPasscodeButton = Button.byText(page, "Try another passcode")

    fun clickTryAnotherPasscode(): PasscodeEntryPage {
        tryAnotherPasscodeButton.clickAndWait()
        return createValidPage(page, PasscodeEntryPage::class)
    }
}
