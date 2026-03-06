package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ExemptionFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionStep

class EicrExemptionPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : ExemptionFormPage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${EicrExemptionStep.ROUTE_SEGMENT}",
    )
