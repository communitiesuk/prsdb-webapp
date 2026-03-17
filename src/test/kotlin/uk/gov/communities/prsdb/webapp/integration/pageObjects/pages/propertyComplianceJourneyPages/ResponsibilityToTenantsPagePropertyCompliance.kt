package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DeclarationBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.ResponsibilityToTenantsStep

class ResponsibilityToTenantsPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : DeclarationBasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ResponsibilityToTenantsStep.ROUTE_SEGMENT}",
    )
