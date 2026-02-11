package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.IncompletePropertiesController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class DeleteIncompletePropertyRegistrationConfirmationPage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        IncompletePropertiesController.getDeleteIncompletePropertyConfirmationPath(urlArguments["contextId"]!!),
    ) {
    val returnToIncompleteProperties = Button.byText(page, "Return to incomplete properties")
}
