package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.AreYouSureFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureStep

class AreYouSureFormPagePropertyDeregistration(
    page: Page,
    urlArguments: Map<String, String>,
) : AreYouSureFormBasePage(
        page,
        DeregisterPropertyController.getPropertyDeregistrationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${AreYouSureStep.ROUTE_SEGMENT}",
    )
