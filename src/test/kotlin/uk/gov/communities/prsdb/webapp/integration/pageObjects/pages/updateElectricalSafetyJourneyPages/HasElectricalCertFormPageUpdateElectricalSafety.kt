package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep

class HasElectricalCertFormPageUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${HasElectricalCertStep.ROUTE_SEGMENT}",
    ) {
    val form = HasElectricalCertForm(page)

    fun submitHasEic() {
        form.electricalCertTypeRadios.selectValue("HAS_EIC")
        form.submit()
    }

    fun submitHasEicr() {
        form.electricalCertTypeRadios.selectValue("HAS_EICR")
        form.submit()
    }

    fun submitHasNoCert() {
        form.electricalCertTypeRadios.selectValue("NO_CERTIFICATE")
        form.submit()
    }

    class HasElectricalCertForm(
        page: Page,
    ) : PostForm(page) {
        val electricalCertTypeRadios = Radios(locator)
    }
}
