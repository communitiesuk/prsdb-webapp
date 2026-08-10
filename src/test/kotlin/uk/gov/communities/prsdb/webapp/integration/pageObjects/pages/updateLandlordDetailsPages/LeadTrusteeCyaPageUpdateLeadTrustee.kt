package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController.Companion.UPDATE_LEAD_TRUSTEE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.leadTrustee.UpdateLeadTrusteeCyaStep

class LeadTrusteeCyaPageUpdateLeadTrustee(
    page: Page,
) : BasePage(page, "$UPDATE_LEAD_TRUSTEE_ROUTE/${UpdateLeadTrusteeCyaStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)

    fun submitAndContinue() {
        form.submit()
    }
}
