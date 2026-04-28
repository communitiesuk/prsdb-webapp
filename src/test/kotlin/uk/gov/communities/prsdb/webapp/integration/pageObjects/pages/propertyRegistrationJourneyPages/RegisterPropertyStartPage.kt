package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class RegisterPropertyStartPage(
    page: Page,
) : BasePage(page, RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE) {
    val heading: Locator? = page.locator(".govuk-heading-l")
    val startButton = Button.byText(page, "Continue")
    val occupiedHeading: Locator = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("If your property is occupied"))
    val jointLandlordsHeading: Locator =
        page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("If there are other landlords (‘joint landlords’)"),
        )
    val afterRegisteredHeading: Locator = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("After you’ve registered"))
    val joinPropertyLink: Locator = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("join a registered property instead"))
}
