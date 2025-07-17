package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ExemptionReasonFormPage

class GasSafetyExemptionReasonPagePropertyComplianceUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : ExemptionReasonFormPage<GasSafetyExemptionReason>(
        page,
        PropertyComplianceController.getUpdatePropertyComplianceStepPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
            PropertyComplianceStepId.GasSafetyExemptionReason,
        ),
    )
