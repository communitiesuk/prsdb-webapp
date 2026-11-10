package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.AreYouSureFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmRemoveSuperiorLandlordStep

class ConfirmRemoveSuperiorLandlordFormPagePropertyRegistration(
    page: Page,
) : AreYouSureFormBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${ConfirmRemoveSuperiorLandlordStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
}
