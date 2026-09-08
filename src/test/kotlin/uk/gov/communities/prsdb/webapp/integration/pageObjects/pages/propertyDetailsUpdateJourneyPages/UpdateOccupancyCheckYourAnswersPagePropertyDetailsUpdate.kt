package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyCheckYourAnswersStep

class UpdateOccupancyCheckYourAnswersPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateOccupancyController.getUpdateOccupancyRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdateOccupancyCheckYourAnswersStep.ROUTE_SEGMENT}",
    ) {
    val form = Form(page)

    val summaryList = OccupancyCheckYourAnswersSummaryList(page)

    fun confirm() = form.submit()

    fun clickChangeOccupancy() = summaryList.occupancyRow.clickFirstActionLinkAndWait()

    class OccupancyCheckYourAnswersSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val occupancyRow = getRow("Occupied by tenants")
    }
}
