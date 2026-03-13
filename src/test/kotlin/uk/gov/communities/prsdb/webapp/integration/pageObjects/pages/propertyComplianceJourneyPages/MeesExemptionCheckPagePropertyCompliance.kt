package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.MeesExemptionCheckBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionCheckStep

class MeesExemptionCheckPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : MeesExemptionCheckBasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${MeesExemptionCheckStep.ROUTE_SEGMENT}",
    )
