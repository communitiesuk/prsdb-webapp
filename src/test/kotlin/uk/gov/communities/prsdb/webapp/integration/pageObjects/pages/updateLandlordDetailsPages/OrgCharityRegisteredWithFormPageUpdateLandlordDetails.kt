package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep

class OrgCharityRegisteredWithFormPageUpdateLandlordDetails(
    page: Page,
) : BasePage(page, "$UPDATE_ORG_CHARITY_ROUTE/${OrgCharityRegisteredWithStep.ROUTE_SEGMENT}") {
    val form = CharityRegisteredWithForm(page)

    fun submitCharityRegisteredWith(charityRegulator: CharityRegulator) {
        form.charityRegisteredWithRadios.selectValue(charityRegulator.name)
        form.submit()
    }

    class CharityRegisteredWithForm(
        page: Page,
    ) : PostForm(page) {
        val charityRegisteredWithRadios = Radios(locator)
    }
}
