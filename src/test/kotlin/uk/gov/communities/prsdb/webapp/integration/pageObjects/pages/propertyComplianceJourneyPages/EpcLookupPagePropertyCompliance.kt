package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.SearchForEpcStep

class EpcLookupPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : EpcLookupBasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${SearchForEpcStep.ROUTE_SEGMENT}",
    )
