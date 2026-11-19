package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateFreeholdersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Warning
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SummaryCardListFormBasePage

class FreeholdersListFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : SummaryCardListFormBasePage(
        page,
        UpdateFreeholdersController.getUpdateFreeholdersFirstStepRoute(urlArguments["propertyOwnershipId"]!!.toLong()),
    ) {
    val warning = Warning.default(page)

    fun getSummaryCard(title: String) = SummaryCard(page, title)

    val removeActionCount: Int
        get() = page.locator(".govuk-summary-card__action:has-text('Remove')").count()
}
