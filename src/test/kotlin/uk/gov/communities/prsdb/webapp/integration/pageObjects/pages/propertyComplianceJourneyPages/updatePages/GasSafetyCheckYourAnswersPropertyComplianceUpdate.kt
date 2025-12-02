package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
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
    val form = Form(page)

    val summaryList = CheckGasSafetyComplianceUpdateSummaryList(page)

    fun confirm() = form.submit()

    class CheckGasSafetyComplianceUpdateSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val gasSafetyRow = getRow("Gas safety certificate")
        val issueDateRow = getRow("Issue date")
        val engineerRow = getRow("Gas Safe engineer number")
        val exemptionRow = getRow("Exemption")
    }
}
