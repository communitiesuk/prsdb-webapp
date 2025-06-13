package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class MeesExemptionReasonPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.MeesExemptionReason.urlPathSegment}",
    ) {
    val form = MeesExemptionReasonForm(page)

    fun submitExemptionReason(exemptionReason: MeesExemptionReason) {
        form.exemptionReasonRadios.selectValue(exemptionReason)
        form.submit()
    }

    class MeesExemptionReasonForm(
        page: Page,
    ) : PostForm(page) {
        val exemptionReasonRadios = Radios(locator)
    }
}
