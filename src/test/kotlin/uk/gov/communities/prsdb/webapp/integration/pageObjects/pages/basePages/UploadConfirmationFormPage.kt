package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button

abstract class UploadConfirmationFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading: Locator = page.locator(".govuk-heading-l")
    val saveAndContinueButton = Button.byText(page, "Save and continue")
}
