package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateSuperiorLandlordsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.superiorLandlords.UpdateSuperiorLandlordsCheckYourAnswersStep

class UpdateSuperiorLandlordsCheckYourAnswersPageSuperiorLandlordsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateSuperiorLandlordsController.getUpdateSuperiorLandlordsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdateSuperiorLandlordsCheckYourAnswersStep.ROUTE_SEGMENT}",
    ) {
    override val expectedTitleHeading = "Check your ownership chain answers"

    val form = Form(page)

    val summaryList = SuperiorLandlordsCheckYourAnswersSummaryList(page)

    fun confirm() = form.submit()

    fun clickChangeAndWait() = summaryList.anyoneInOwnershipChainRow.clickFirstActionLinkAndWait()

    class SuperiorLandlordsCheckYourAnswersSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val anyoneInOwnershipChainRow = getRow("Is there anyone else in the property’s ownership chain?")
    }
}
