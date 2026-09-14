package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateLicensingJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateLicensingController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingCyaStep
import java.util.UUID

class CheckLicensingAnswersPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        LettingAgentUpdateLicensingController.getUpdateLicensingRoute(UUID.fromString(urlArguments["token"]!!)) +
            "/${UpdateLicensingCyaStep.ROUTE_SEGMENT}",
    ) {
    val form = Form(page)

    val summaryName = Heading(page.locator("#summary-name"))
    val summaryList = CheckLicensingAnswersLettingAgentSummaryList(page)

    fun confirm() = form.submit()

    class CheckLicensingAnswersLettingAgentSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val licensingTypeRow = getRow("Licensing type")
        val licensingNumberRow = getRow("Licensing number")
    }
}
