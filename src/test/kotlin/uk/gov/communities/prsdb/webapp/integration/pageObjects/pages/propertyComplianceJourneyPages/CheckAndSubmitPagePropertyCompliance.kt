package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckAndSubmitPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.CheckAndSubmit.urlPathSegment}",
    ) {
    val form = CheckAndSubmitPagePropertyComplianceForm(page)

    class CheckAndSubmitPagePropertyComplianceForm(
        page: Page,
    ) : PostForm(page) {
        val gasSummaryList = GasSummaryList(page)
        val eicrSummaryList = EicrSummaryList(page)
    }

    class GasSummaryList(
        page: Page,
    ) : SummaryList(page, index = 0) {
        val statusRow = SummaryListRow.byKey(locator, "Gas safety certificate")
        val engineerNumRow = SummaryListRow.byKey(locator, "Gas Safe engineer number")
    }

    class EicrSummaryList(
        page: Page,
    ) : SummaryList(page, index = 1) {
        val statusRow = SummaryListRow.byKey(locator, "Electrical installation condition report (EICR)")
        val issueDateRow = SummaryListRow.byKey(locator, "Issue date")
    }
}
