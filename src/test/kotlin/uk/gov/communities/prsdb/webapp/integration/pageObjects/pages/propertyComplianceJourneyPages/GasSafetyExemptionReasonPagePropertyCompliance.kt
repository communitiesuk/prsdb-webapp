package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.NewPropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ExemptionReasonFormPage

class GasSafetyExemptionReasonPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : ExemptionReasonFormPage<GasSafetyExemptionReason>(
        page,
        NewPropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.GasSafetyExemptionReason.urlPathSegment}",
    )
