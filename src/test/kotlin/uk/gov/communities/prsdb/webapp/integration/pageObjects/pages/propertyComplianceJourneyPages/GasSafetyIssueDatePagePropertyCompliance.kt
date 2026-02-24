package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep

class GasSafetyIssueDatePagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : DateFormPage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${GasSafetyIssueDateStep.ROUTE_SEGMENT}",
    )
