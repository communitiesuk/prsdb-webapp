package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.AreYouSureStep

class AreYouSureFormPageLandlordDeregistration(
    page: Page,
) : BasePage(
        page,
        "${DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE}/${AreYouSureStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val yesDeleteButton = Button.byText(page, "Yes, delete")
    val form = PostForm(page)
    val backLink = BackLink.default(page)
    val cancelLink = Link.byText(page, "Cancel and go back")

    fun submitYesDelete() {
        form.submit()
    }
}
