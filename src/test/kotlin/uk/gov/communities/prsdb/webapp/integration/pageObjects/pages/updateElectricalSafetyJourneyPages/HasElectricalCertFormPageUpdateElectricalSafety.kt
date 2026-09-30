package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasElectricalCertFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep

class HasElectricalCertFormPageUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : HasElectricalCertFormBasePage(
        page,
        LandlordUpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${HasElectricalCertStep.ROUTE_SEGMENT}",
    )
