package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class GasSafetyExemptionReasonPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.GasSafetyExemptionReason.urlPathSegment}",
    ) {
    val form = ExemptionReasonForm(page)

    fun submitExemptionReason(exemptionReason: GasSafetyExemptionReason) {
        form.exemptionReasonRadios.selectValue(exemptionReason)
        form.submit()
    }

    class ExemptionReasonForm(
        page: Page,
    ) : Form(page) {
        val exemptionReasonRadios = Radios(locator)
    }
}
