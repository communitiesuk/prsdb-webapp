package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner

abstract class DeleteLocalCouncilUserSuccessBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val confirmationBanner = ConfirmationBanner(page)
    val returnButton = Button.byText(page, "Return to manage users")
}
