package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ErrorSummary
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.NotificationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class GeneratePassphrasePage(
    page: Page,
) : BasePage(page, "generate-passphrase") {
    val heading = Heading(page.locator("main h1"))
    val generateAnotherButton = Button.byText(page, "Generate another passphrase")
    val emailForm = EmailForm(page.locator("main"))
    val smsForm = SmsForm(page.locator("main"))
    val errorSummary = ErrorSummary(page)
    val notificationStatus: Locator = page.locator("[role='status']")
    val successBanner = NotificationBanner(page)
    val readBackGuidance = page.locator("#passphrase-read-back")
    val letterGuidance = page.locator("#passphrase-letter-guidance")
    val passphrase: String
        get() = page.locator("#passphrase-preview strong").textContent()

    class EmailForm(
        parentLocator: Locator,
    ) : PostForm(parentLocator, "form:has(input[name='channel'][value='email'])") {
        val emailAddress = TextInput.emailByFieldName(locator, "emailAddress")
        val passphraseToken = TextInput.hiddenByFieldName(locator, "passphraseToken")
    }

    class SmsForm(
        parentLocator: Locator,
    ) : PostForm(parentLocator, "form:has(input[name='channel'][value='sms'])") {
        val phoneNumber = TextInput.textByFieldName(locator, "phoneNumber")
        val passphraseToken = TextInput.hiddenByFieldName(locator, "passphraseToken")
    }
}
