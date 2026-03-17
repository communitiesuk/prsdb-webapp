package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckMatchedEpcBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStep

class CheckAutoMatchedEpcPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckMatchedEpcBasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${CheckMatchedEpcStep.AUTOMATCHED_ROUTE_SEGMENT}",
    )
