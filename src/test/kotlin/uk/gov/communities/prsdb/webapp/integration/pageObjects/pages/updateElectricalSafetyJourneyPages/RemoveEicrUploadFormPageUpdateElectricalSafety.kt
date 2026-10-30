package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithRadios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveEicrUploadStep

class RemoveEicrUploadFormPageUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        LandlordUpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${RemoveEicrUploadStep.ROUTE_SEGMENT}",
    ) {
    val form = FormWithRadios(page)
}
