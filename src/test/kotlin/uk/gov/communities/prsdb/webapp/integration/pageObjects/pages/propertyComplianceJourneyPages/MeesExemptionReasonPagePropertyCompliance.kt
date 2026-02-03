package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.NewPropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.MeesExemptionReasonBasePage

class MeesExemptionReasonPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : MeesExemptionReasonBasePage(
        page,
        NewPropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.MeesExemptionReason.urlPathSegment}",
    )
