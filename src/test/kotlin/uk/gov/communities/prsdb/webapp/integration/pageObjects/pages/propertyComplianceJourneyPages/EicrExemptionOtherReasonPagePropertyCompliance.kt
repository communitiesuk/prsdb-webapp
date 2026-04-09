package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ReasonFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionOtherReasonStep

class EicrExemptionOtherReasonPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : ReasonFormPage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${EicrExemptionOtherReasonStep.ROUTE_SEGMENT}",
    )
