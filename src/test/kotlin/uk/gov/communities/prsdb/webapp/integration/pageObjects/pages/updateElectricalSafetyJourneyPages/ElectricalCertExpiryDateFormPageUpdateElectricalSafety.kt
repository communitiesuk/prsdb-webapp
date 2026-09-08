package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep

class ElectricalCertExpiryDateFormPageUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : DateFormPage(
        page,
        LandlordUpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${ElectricalCertExpiryDateStep.ROUTE_SEGMENT}",
    )
