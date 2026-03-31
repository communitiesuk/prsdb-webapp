package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcExpiryCheckBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiryCheckStep

class EpcExpiryCheckPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : EpcExpiryCheckBasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${EpcExpiryCheckStep.ROUTE_SEGMENT}",
    )
