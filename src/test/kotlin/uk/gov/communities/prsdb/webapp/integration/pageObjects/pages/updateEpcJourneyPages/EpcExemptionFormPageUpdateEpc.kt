package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep

class EpcExemptionFormPageUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateEpcController.UPDATE_EPC_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${EpcExemptionStep.ROUTE_SEGMENT}",
    ) {
    val form = EpcExemptionForm(page)

    fun submitExemptionReason(reason: String) {
        form.exemptionRadios.selectValue(reason)
        form.submit()
    }

    class EpcExemptionForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val exemptionRadios = Radios(locator)
    }
}
