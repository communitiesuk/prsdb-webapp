package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalCertUploadsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicrUploadsStep

class CheckEicrUploadsFormPageUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckElectricalCertUploadsFormBasePage(
        page,
        LandlordUpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${CheckEicrUploadsStep.ROUTE_SEGMENT}",
    )
