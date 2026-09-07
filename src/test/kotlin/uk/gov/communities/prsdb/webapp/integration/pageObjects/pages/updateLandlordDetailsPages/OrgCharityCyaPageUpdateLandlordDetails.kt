package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationCharity.UpdateOrganisationCharityCyaStep

class OrgCharityCyaPageUpdateLandlordDetails(
    page: Page,
) : BasePage(page, "$UPDATE_ORG_CHARITY_ROUTE/${UpdateOrganisationCharityCyaStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)
    val summaryList = CharityCyaSummaryList(page)
    val mainContent: Locator = page.locator("main")

    fun submit() {
        form.submit()
    }

    class CharityCyaSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val registeredCharityRow = getRow("Registered charity")
        val charityCommissionRow = getRow("Charity commission")
        val charityNumberRow = getRow("Charity number")
    }
}
