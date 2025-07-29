package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class EicrCheckYourAnswersPagePropertyComplianceUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getUpdatePropertyComplianceStepPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
            PropertyComplianceStepId.UpdateEicrCheckYourAnswers,
        ),
    ) {
    val form = EicrCheckYourAnswersForm(page)

    class EicrCheckYourAnswersForm(
        page: Page,
    ) : Form(page) {
        val summaryList = EicrCheckYourAnswersSummaryList(locator)
    }

    class EicrCheckYourAnswersSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val eicrRow = getRow("Electrical Installation Condition Report")
        val issueDateRow = getRow("Issue date")
        val exemptionRow = getRow("Exemption")
    }
}
