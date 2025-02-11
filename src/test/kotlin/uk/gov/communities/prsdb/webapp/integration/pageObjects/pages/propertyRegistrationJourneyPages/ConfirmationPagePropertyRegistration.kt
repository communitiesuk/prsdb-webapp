package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPagePropertyRegistration(
    page: Page,
) : BasePage(page, "$REGISTER_PROPERTY_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT") {
    private val detailTable = Table(page)
    val registrationNumberText: String = detailTable.getCell(0, 1).innerText()

    fun clickGoToDashboard() = clickComponent(BaseComponent.getButton(page, "Go to Dashboard"))
}
