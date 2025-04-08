package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class GasSafetyPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.GasSafety.urlPathSegment}",
    ) {
    val form = GasSafetyForm(page)

    fun submitHasGasSafetyCert() {
        form.hasGasSafetyCertRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNoGasSafetyCert() {
        form.hasGasSafetyCertRadios.selectValue("false")
        form.submit()
    }

    class GasSafetyForm(
        page: Page,
    ) : Form(page) {
        val hasGasSafetyCertRadios = Radios(locator)
    }
}
