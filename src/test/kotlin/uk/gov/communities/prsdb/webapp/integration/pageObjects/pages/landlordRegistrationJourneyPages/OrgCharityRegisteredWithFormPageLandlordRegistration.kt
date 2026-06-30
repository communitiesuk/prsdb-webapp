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
    val detailsSummary: Locator = page.locator(".govuk-details__summary-text")
    val detailsText: Locator = page.locator(".govuk-details__text")
    val radiosDivider: Locator = page.locator(".govuk-radios__divider")
    val form = CharityRegisteredWithForm(page)

    fun getRadioLabel(charityRegulator: CharityRegulator): Locator =
        page.locator("label[for='charityRegisteredWith-${charityRegulator.name}']")

    fun submitCharityRegisteredWith(charityRegulator: CharityRegulator) {
        form.charityRegisteredWithRadios.selectValue(charityRegulator)
        form.submit()
    }

    class CharityRegisteredWithForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val charityRegisteredWithRadios = Radios(locator)
    }
}
