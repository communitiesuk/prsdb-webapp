package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PasscodeEntryPage(
    page: Page,
) : BasePage(page, "passcode-entry") {
    val heading = Heading(page.locator("main h1"))
    val description = page.locator("p.govuk-body")
    val form = PasscodeForm(page)
    val backLink = BackLink.default(page)

    fun submitPasscode(passcode: String) {
        form.passcodeInput.fill(passcode)
        form.submit()
    }

    class PasscodeForm(
        page: Page,
    ) : Form(page) {
        val passcodeInput = TextInput.textByFieldName(locator, "passcode")
    }
}
