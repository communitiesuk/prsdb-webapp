package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LegacyPropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class EicrCheckYourAnswersPagePropertyComplianceUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        LegacyPropertyComplianceController.getUpdatePropertyComplianceStepPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
            PropertyComplianceStepId.UpdateEicrCheckYourAnswers,
        ),
    ) {
    val form = Form(page)

    val summaryList = EicrCheckYourAnswersSummaryList(page)

    class EicrCheckYourAnswersSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val eicrRow = getRow("Electrical Installation Condition Report")
        val issueDateRow = getRow("Issue date")
        val exemptionRow = getRow("Exemption")
    }
}
