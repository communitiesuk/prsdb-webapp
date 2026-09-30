package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ElectricalCertMissingFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep

class ElectricalCertMissingFormPageUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : ElectricalCertMissingFormBasePage(
        page,
        LandlordUpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${ElectricalCertMissingStep.ROUTE_SEGMENT}",
    )
