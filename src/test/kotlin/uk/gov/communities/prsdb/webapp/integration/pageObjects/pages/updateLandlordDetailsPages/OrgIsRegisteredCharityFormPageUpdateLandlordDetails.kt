package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep

class OrgIsRegisteredCharityFormPageUpdateLandlordDetails(
    page: Page,
) : BasePage(page, "$UPDATE_ORG_CHARITY_ROUTE/${OrgIsRegisteredCharityStep.ROUTE_SEGMENT}") {
    val heading: Locator = page.locator("h1")
    val form = OrgIsRegisteredCharityForm(page)

    fun submitYes() {
        form.charityRadios.selectValue("true")
        form.submit()
    }

    fun submitNo() {
        form.charityRadios.selectValue("false")
        form.submit()
    }

    class OrgIsRegisteredCharityForm(
        page: Page,
    ) : PostForm(page) {
        val charityRadios = Radios(locator)
    }
}
