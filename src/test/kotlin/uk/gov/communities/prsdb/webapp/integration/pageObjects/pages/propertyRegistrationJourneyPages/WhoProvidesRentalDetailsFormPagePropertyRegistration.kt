package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsStep

class WhoProvidesRentalDetailsFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${WhoProvidesRentalDetailsStep.ROUTE_SEGMENT}",
    ) {
    val form = WhoProvidesRentalDetailsForm(page)

    fun submitLandlordProvidesDetails() {
        form.whoProvidesRadios.selectValue(WhoProvidesRentalDetails.LANDLORD)
        form.submit()
    }

    fun submitLettingAgentProvidesDetails() {
        form.whoProvidesRadios.selectValue(WhoProvidesRentalDetails.LETTING_AGENT)
        form.submit()
    }

    class WhoProvidesRentalDetailsForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val whoProvidesRadios = Radios(locator)
    }
}
