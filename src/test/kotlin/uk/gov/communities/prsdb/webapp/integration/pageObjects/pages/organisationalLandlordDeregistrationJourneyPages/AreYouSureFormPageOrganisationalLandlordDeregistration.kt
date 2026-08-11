package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationalLandlordDeregistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeregisterOrganisationalLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig.AreYouSureStep

class AreYouSureFormPageOrganisationalLandlordDeregistration(
    page: Page,
) : BasePage(
        page,
        "${DeregisterOrganisationalLandlordController.ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE}/${AreYouSureStep.ROUTE_SEGMENT}",
    ) {
    val heading: Locator = page.locator("h1")
    val form = PostForm(page)
    val backLink = BackLink.default(page)
    val cancelLink = Link.byText(page, "Cancel and go back")

    fun submitYesDelete() {
        form.submit()
    }
}
