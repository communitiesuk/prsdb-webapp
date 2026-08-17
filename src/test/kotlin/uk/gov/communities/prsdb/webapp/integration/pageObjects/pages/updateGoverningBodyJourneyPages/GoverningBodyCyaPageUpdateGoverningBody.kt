package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateGoverningBodyController.Companion.UPDATE_GOVERNING_BODY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody.UpdateGoverningBodyCyaStep

class GoverningBodyCyaPageUpdateGoverningBody(
    page: Page,
) : BasePage(page, "$UPDATE_GOVERNING_BODY_ROUTE/${UpdateGoverningBodyCyaStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)

    fun submit() {
        form.submit()
    }
}
