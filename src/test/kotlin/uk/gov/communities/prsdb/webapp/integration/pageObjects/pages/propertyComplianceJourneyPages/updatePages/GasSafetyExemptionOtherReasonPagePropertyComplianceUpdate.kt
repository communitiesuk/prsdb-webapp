package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LegacyPropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ReasonFormPage

class GasSafetyExemptionOtherReasonPagePropertyComplianceUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : ReasonFormPage(
        page,
        LegacyPropertyComplianceController.getUpdatePropertyComplianceStepPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
            PropertyComplianceStepId.GasSafetyExemptionOtherReason,
        ),
    )
