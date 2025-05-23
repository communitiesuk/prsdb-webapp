package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckOccupancyAnswersPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyDetailsController.getUpdatePropertyDetailsPath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers.urlPathSegment}",
    ) {
    val form = CheckOccupancyAnswersPropertyDetailsUpdateForm(page)

    fun confirm() = form.submit()

    class CheckOccupancyAnswersPropertyDetailsUpdateForm(
        page: Page,
    ) : Form(page) {
        val summaryList = CheckLicensingAnswersPropertyDetailsSummaryList(locator)
    }

    class CheckLicensingAnswersPropertyDetailsSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val occupancyRow = getRow("Is your property occupied by tenants?")
        val numberOfHouseholdsRow = getRow("How many households live in your property?")
        val numberOfPeopleRow = getRow("How many people live in your property?")
    }
}
