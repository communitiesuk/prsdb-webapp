package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep

class OrgNameFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgNameStep.ROUTE_SEGMENT}") {
    val form = OrgNameForm(page)

    fun submitName(name: String) {
        form.orgNameInput.fill(name)
        form.submit()
    }

    class OrgNameForm(
        page: Page,
    ) : PostForm(page) {
        val orgNameInput = TextInput.textByFieldName(locator, "orgName")
    }
}
