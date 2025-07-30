package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class UpdateEpcCheckYourAnswersPagePropertyComplianceUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getUpdatePropertyComplianceStepPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
            PropertyComplianceStepId.UpdateEpcCheckYourAnswers,
        ),
    ) {
    val form = EpcCheckYourAnswersForm(page)

    class EpcCheckYourAnswersForm(
        page: Page,
    ) : Form(page) {
        val summaryList = EpcCheckYourAnswersSummaryList(locator)
    }

    class EpcCheckYourAnswersSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val epcRow = getRow("Energy Performance Certificate")
        val expiryDateRow = getRow("Expiry date")
        val expiryCheckRow = getRow("Did the tenancy start before the EPC expired")
        val energyRatingRow = getRow("Energy rating")
        val meesExemptionRow = getRow("MEES exemption")
        val exemptionReasonRow = getRow("EPC exemption")
    }
}
