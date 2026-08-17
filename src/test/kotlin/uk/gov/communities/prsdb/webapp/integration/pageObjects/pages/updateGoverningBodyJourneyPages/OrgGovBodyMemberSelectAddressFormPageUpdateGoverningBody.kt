package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateGoverningBodyController.Companion.UPDATE_GOVERNING_BODY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.GovBodyMemberAddressTask

class OrgGovBodyMemberSelectAddressFormPageUpdateGoverningBody(
    page: Page,
) : SelectAddressFormPage(
        page,
        "$UPDATE_GOVERNING_BODY_ROUTE/${GovBodyMemberAddressTask.ROUTE_SEGMENT}/${SelectAddressStep.ROUTE_SEGMENT}",
    ) {
    val heading: Locator = page.locator("h1")
}
