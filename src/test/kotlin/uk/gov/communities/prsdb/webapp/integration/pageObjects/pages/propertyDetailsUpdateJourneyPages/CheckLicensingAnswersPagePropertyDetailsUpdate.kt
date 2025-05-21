package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckLicensingAnswersPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyDetailsController.getUpdatePropertyDetailsPath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdatePropertyDetailsStepId.CheckYourLicensingAnswers.urlPathSegment}",
    ) {
    val form = CheckLicensingAnswersPropertyDetailsUpdateForm(page)

    fun confirm() = form.submit()

    class CheckLicensingAnswersPropertyDetailsUpdateForm(
        page: Page,
    ) : Form(page) {
        val summaryName = Heading(page.locator("form h2"))
        val summaryList = CheckLicensingAnswersPropertyDetailsSummaryList(locator)
    }

    class CheckLicensingAnswersPropertyDetailsSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val licensingRow = getRow("Licensing type")
    }
}
