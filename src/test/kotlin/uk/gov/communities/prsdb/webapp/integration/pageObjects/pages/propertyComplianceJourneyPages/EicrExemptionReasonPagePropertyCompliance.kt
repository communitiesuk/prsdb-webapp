package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ExemptionReasonFormPage

class EicrExemptionReasonPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : ExemptionReasonFormPage<EicrExemptionReason>(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.EicrExemptionReason.urlPathSegment}",
    )
