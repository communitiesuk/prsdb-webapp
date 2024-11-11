package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertEquals
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.ConfirmationPageBanner
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class InviteNewLaUserSuccessPage(
    page: Page,
) : BasePage(page) {
    override fun validate() {
        assertEquals("Invite sent", page.title())
    }

    val confirmationBanner: ConfirmationPageBanner = ConfirmationPageBanner(page.locator(".govuk-panel--confirmation"))
}
