package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalSafetyAnswersFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep

class CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckElectricalSafetyAnswersFormBasePage(
        page,
        UpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT}",
    )
