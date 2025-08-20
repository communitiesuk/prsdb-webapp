package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.PASSCODE_ENTRY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PasscodeEntryPage(
    page: Page,
) : BasePage(page, PASSCODE_ENTRY_PATH_SEGMENT) {
    val form = PasscodeForm(page)

    fun submitPasscode(passcode: String) {
        form.passcodeInput.fill(passcode)
        form.submit()
    }

    class PasscodeForm(
        page: Page,
    ) : PostForm(page) {
        val passcodeInput = TextInput.textByFieldName(locator, "passcode")
    }
}
