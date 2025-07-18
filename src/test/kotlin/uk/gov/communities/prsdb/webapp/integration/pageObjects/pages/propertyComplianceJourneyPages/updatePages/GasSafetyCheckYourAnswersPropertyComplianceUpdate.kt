package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class GasSafetyCheckYourAnswersPropertyComplianceUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getUpdatePropertyComplianceStepPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
            PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
        ),
    ) {
    val form = CheckGasSafetyComplianceUpdateForm(page)

    fun confirm() = form.submit()

    class CheckGasSafetyComplianceUpdateForm(
        page: Page,
    ) : Form(page) {
        val summaryName = Heading(page.locator("form h2"))
        val summaryList = CheckGasSafetyComplianceUpdateSummaryList(locator)
    }

    class CheckGasSafetyComplianceUpdateSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val gasSafetyRow = getRow("Gas safety certificate")
    }
}
