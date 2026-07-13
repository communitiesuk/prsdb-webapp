package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyDetailsStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsMode

class OrgGovBodyDetailsFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgGovBodyDetailsStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = OrgGovBodyDetailsForm(page)

    fun submitHasDetails() {
        form.submitSelectedButton(OrgGovBodyDetailsMode.HAS_DETAILS.name)
    }

    fun submitNoDetails() {
        form.submitSelectedButton(OrgGovBodyDetailsMode.NO_DETAILS.name)
    }

    class OrgGovBodyDetailsForm(
        page: Page,
    ) : PostForm(page)
}
