package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdatePropertyManagerController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager.UpdatePropertyManagerCheckYourAnswersStep

class UpdatePropertyManagerCheckYourAnswersPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdatePropertyManagerController.getUpdatePropertyManagerRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdatePropertyManagerCheckYourAnswersStep.ROUTE_SEGMENT}",
    ) {
    override val expectedTitleHeading = "Check your property manager answers"

    val form = Form(page)

    val summaryList = PropertyManagerCheckYourAnswersSummaryList(page)

    fun confirm() = form.submit()

    fun clickChangeAndWait() = summaryList.usingPropertyManagerRow.clickFirstActionLinkAndWait()

    class PropertyManagerCheckYourAnswersSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val usingPropertyManagerRow = getRow("Are you using a property manager?")
    }
}
