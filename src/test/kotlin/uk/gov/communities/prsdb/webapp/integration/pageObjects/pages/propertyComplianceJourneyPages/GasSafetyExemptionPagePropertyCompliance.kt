package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class GasSafetyExemptionPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.GasSafetyExemption.urlPathSegment}",
    ) {
    val form = GasSafetyExemptionForm(page)

    fun submitHasGasSafetyCertExemption() {
        form.hasGasSafetyCertExemptionRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNoGasSafetyCertExemption() {
        form.hasGasSafetyCertExemptionRadios.selectValue("false")
        form.submit()
    }

    class GasSafetyExemptionForm(
        page: Page,
    ) : Form(page) {
        val hasGasSafetyCertExemptionRadios = Radios(locator)
    }
}
