package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UpdateEpcCheckYourAnswersBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep

class CheckEpcAnswersFormPageUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : UpdateEpcCheckYourAnswersBasePage(
        page,
        UpdateEpcController.UPDATE_EPC_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${CheckEpcAnswersStep.ROUTE_SEGMENT}",
    )
