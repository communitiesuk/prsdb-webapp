package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep

class OrgCharityRegisteredWithFormPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgCharityRegisteredWithStep.ROUTE_SEGMENT}",
    ) {
    val heading: Locator = page.locator("h1")
    val form = CharityRegisteredWithForm(page)

    fun submitCharityRegisteredWith(charityRegulator: CharityRegulator) {
        form.charityRegisteredWithRadios.selectValue(charityRegulator.name)
        form.submit()
    }

    class CharityRegisteredWithForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val charityRegisteredWithRadios = Radios(locator)
    }
}
