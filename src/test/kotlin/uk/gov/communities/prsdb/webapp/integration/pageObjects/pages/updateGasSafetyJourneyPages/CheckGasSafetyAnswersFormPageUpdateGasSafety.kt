package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep

class CheckGasSafetyAnswersFormPageUpdateGasSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateGasSafetyController.UPDATE_GAS_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${CheckGasSafetyAnswersStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val form = Form(page)
    val gasSupplySummaryList = GasSupplySummaryList(page)

    fun confirm() = form.submit()

    class GasSupplySummaryList(
        page: Page,
    ) : SummaryList(page, 0) {
        val gasSupplyRow = getRow("Does the property have a gas supply or any gas appliances?")
    }
}
