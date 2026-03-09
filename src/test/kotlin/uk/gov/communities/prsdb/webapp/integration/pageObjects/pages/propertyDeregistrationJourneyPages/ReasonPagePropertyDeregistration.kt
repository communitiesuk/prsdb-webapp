package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ReasonFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.ReasonStep

class ReasonPagePropertyDeregistration(
    page: Page,
    urlArguments: Map<String, String>,
) : ReasonFormPage(
        page,
        DeregisterPropertyController.getPropertyDeregistrationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ReasonStep.ROUTE_SEGMENT}",
    )
