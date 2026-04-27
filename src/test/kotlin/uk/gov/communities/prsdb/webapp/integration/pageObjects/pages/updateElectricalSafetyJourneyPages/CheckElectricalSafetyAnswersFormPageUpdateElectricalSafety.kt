package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep

class CheckElectricalSafetyAnswersFormPageUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT}",
    ) {
    val form = PostForm(page)
    val summaryList = ElectricalSafetySummaryList(page)

    class ElectricalSafetySummaryList(
        page: Page,
    ) : SummaryList(page, 0) {
        val electricalCertRow = getRow("Which electrical safety certificate do you have for this property?")
        val expiryDateRow = getRow("Expiry date")
        val yourCertificateRow = getRow("Your certificate")
    }
}
