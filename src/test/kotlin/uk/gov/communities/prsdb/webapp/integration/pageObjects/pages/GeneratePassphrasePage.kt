package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class GeneratePassphrasePage(
    page: Page,
) : BasePage(page, "generate-passphrase") {
    val heading = Heading(page.locator("main h1"))
    val generateAnotherButton = Button.byText(page, "Generate another passphrase")
    val phoneNumber = TextInput.textByFieldName(page.locator("form"), "phoneNumber")
    val emailAddress = TextInput.emailByFieldName(page.locator("form"), "emailAddress")
    val sendEmailButton = Button.byText(page, "Send email")
    val sendSmsButton = Button.byText(page, "Send text message")
    val notificationStatus = page.locator("[role='status']")
    val letterGuidance = page.locator("#passphrase-letter-guidance")
    val passphrase: String
        get() = page.locator("#passphrase-preview strong").textContent()
}
