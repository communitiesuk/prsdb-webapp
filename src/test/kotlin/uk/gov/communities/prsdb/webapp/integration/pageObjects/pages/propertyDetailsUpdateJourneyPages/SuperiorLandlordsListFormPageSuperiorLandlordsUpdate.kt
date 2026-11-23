package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateSuperiorLandlordsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Warning
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SummaryCardListFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsListStep

class SuperiorLandlordsListFormPageSuperiorLandlordsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : SummaryCardListFormBasePage(
        page,
        UpdateSuperiorLandlordsController.getUpdateSuperiorLandlordsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${SuperiorLandlordsListStep.ROUTE_SEGMENT}",
    ) {
    val warning = Warning.default(page)

    fun getSummaryCard(title: String) = SummaryCard(page, title)
}
