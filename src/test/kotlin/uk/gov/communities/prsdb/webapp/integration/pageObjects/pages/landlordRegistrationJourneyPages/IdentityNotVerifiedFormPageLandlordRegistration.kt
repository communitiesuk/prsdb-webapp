package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityNotVerifiedStep

class IdentityNotVerifiedFormPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${IdentityNotVerifiedStep.ROUTE_SEGMENT}",
    ) {
    val form = FormWithSectionHeader(page)

    fun clickContinue() = form.submit()
}
