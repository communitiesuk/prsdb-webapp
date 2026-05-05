package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep

class HasEpcFormPageUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateEpcController.UPDATE_EPC_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${HasEpcStep.ROUTE_SEGMENT}",
    ) {
    val form = HasEpcForm(page)

    fun submitHasEpc() {
        form.hasCertRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNoEpc() {
        form.hasCertRadios.selectValue("false")
        form.submit()
    }

    class HasEpcForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val hasCertRadios = Radios(locator)
    }
}
