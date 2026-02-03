package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.NewPropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage

class GasSafetyIssueDatePagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : DateFormPage(
        page,
        NewPropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment}",
    )
