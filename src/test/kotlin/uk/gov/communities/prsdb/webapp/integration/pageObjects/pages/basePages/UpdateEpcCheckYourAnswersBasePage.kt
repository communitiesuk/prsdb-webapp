package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

open class UpdateEpcCheckYourAnswersBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = Form(page)

    val summaryList = EpcCheckYourAnswersSummaryList(page)

    class EpcCheckYourAnswersSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val epcRow = getRow("Energy Performance Certificate")
        val expiryDateRow = getRow("Expiry date")
        val expiryCheckRow = getRow("Did the tenancy start before the EPC expired")
        val energyRatingRow = getRow("Energy rating")
        val meesExemptionRow = getRow("MEES exemption")
        val exemptionReasonRow = getRow("EPC exemption")
    }
}
