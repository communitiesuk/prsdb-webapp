package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckEpcAnswersFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep

class CheckEpcAnswersFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckEpcAnswersFormBasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${CheckEpcAnswersStep.ROUTE_SEGMENT}",
    )
