package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ReasonFormPage

class EicrExemptionOtherReasonPagePropertyComplianceUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : ReasonFormPage(
        page,
        PropertyComplianceController.getUpdatePropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.EicrExemptionOtherReason.urlPathSegment}",
    )
